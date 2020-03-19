package ch.bildspur.vision;

import ch.bildspur.vision.network.DeepNeuralNetwork;
import ch.bildspur.vision.result.ObjectDetectionResult;
import ch.bildspur.vision.util.MathUtils;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_text.FloatVector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_dnn.*;

/**
 * Based on https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB/blob/master/caffe/ultra_face_opencvdnn_inference.py
 * Adapted and improved a lot.
 */
public class ULFGFaceDetectionNetwork extends DeepNeuralNetwork<List<ObjectDetectionResult>> {
    private Path modelPath;
    protected Net net;

    private int width;
    private int height;

    private float confidenceThreshold = 0.7f;
    private float iouThreshold = 0.3f;
    private int topK = -1;

    private Scalar imageMean = Scalar.all(127);
    private float imageStd = 128.0f;

    private float centerVariance = 0.1f;
    private float sizeVariance = 0.2f;
    private float[][] minBoxes = {{10.0f, 16.0f, 24.0f}, {32.0f, 48.0f}, {64.0f, 96.0f}, {128.0f, 192.0f, 256.0f}};
    private float[] strides = {8.0f, 16.0f, 32.0f, 64.0f};

    private List<float[]> priors = new ArrayList<>();

    public ULFGFaceDetectionNetwork(Path modelPath, int width, int height) {
        this.modelPath = modelPath;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean setup() {
        net = readNetFromONNX(modelPath.toAbsolutePath().toString());

        if (net.empty()) {
            System.out.println("Can't load network!");
            return false;
        }

        // setup image size
        defineImageSize(new Size(width, height));

        return true;
    }

    @Override
    public List<ObjectDetectionResult> run(Mat frame) {
        // convert image into batch of images
        Mat inputBlob = blobFromImage(frame,
                1 / imageStd,
                new Size(width, height),
                imageMean,
                false, false, CV_32F);

        // set input
        net.setInput(inputBlob);

        // create output layers
        StringVector outNames = net.getUnconnectedOutLayersNames();
        MatVector outs = new MatVector(outNames.size());

        // run detection
        net.forward(outs, outNames);

        // extract boxes and scores
        Mat boxesOut = outs.get(0);
        Mat confidencesOut = outs.get(1);

        // boxes
        Mat boxes = boxesOut.reshape(0, boxesOut.size(1));

        // class confidences (BACKGROUND, face)
        Mat confidences = confidencesOut.reshape(0, confidencesOut.size(1));

        return predict(frame.size().width(), frame.size().height(), confidences, boxes);
    }

    private List<ObjectDetectionResult> predict(int frameWidth, int frameHeight, Mat confidences, Mat boxes) {
        FloatVector relevantConfidences = new FloatVector();
        RectVector relevantBoxes = new RectVector();

        // extract only relevant prob
        for (int i = 0; i < boxes.rows(); i++) {
            FloatPointer confidencesPtr = new FloatPointer(confidences.row(i).data());
            float probability = confidencesPtr.get(1); // read second column (face)

            if (probability < confidenceThreshold) continue;

            // add probability
            relevantConfidences.push_back(probability);

            // add box data and convert locations to positions
            float[] prior = priors.get(i);
            FloatPointer boxesPtr = new FloatPointer(boxes.row(i).data());
            float centerX = ((boxesPtr.get(0) * centerVariance * prior[2] + prior[0]) * frameWidth);
            float centerY = ((boxesPtr.get(1) * centerVariance * prior[3] + prior[1]) * frameHeight);
            float width = (float) ((Math.exp(boxesPtr.get(2) * sizeVariance) * prior[2]) * frameHeight);
            float height = (float) ((Math.exp(boxesPtr.get(3) * sizeVariance) * prior[3]) * frameHeight);

            int left = Math.round(centerX - width / 2.0f);
            int top = Math.round(centerY - height / 2.0f);

            relevantBoxes.push_back(new Rect(left, top, Math.round(width), Math.round(height)));
        }

        // run nms
        IntPointer indices = new IntPointer(confidences.size());
        FloatPointer confidencesPointer = new FloatPointer(relevantConfidences.size());
        confidencesPointer.put(relevantConfidences.get());

        NMSBoxes(relevantBoxes, confidencesPointer, confidenceThreshold, iouThreshold, indices, 1.0f, topK);

        // extract nms result
        List<ObjectDetectionResult> detections = new ArrayList<>();
        for (int i = 0; i < indices.limit(); ++i) {
            int idx = indices.get(i);
            Rect box = relevantBoxes.get(idx);

            detections.add(new ObjectDetectionResult(1, "face", relevantConfidences.get(idx),
                    box.x(), box.y(), box.width(), box.height()));
        }

        return detections;
    }

    private void defineImageSize(Size imageSize) {
        // shrinkageList is always the same
        int[][] featureMapList = new int[2][strides.length];

        // create feature maps
        for (int d = 0; d < featureMapList.length; d++) {
            int size = imageSize.get(d);

            for (int i = 0; i < strides.length; i++) {
                featureMapList[d][i] = (int) (Math.ceil(size / strides[i]));
            }
        }

        generatePriors(featureMapList, imageSize);
    }

    private void generatePriors(int[][] featureMapList, Size imageSize) {
        priors.clear();

        for (int index = 0; index < featureMapList[0].length; index++) {
            float scaleW = imageSize.get(0) / strides[index];
            float scaleH = imageSize.get(1) / strides[index];

            for (int j = 0; j < featureMapList[1][index]; j++) {
                for (int i = 0; i < featureMapList[0][index]; i++) {
                    float xCenter = (i + 0.5f) / scaleW;
                    float yCenter = (j + 0.5f) / scaleH;

                    for (float minBox : minBoxes[index]) {
                        float w = minBox / imageSize.get(0);
                        float h = minBox / imageSize.get(1);

                        priors.add(new float[]{
                                        MathUtils.clamp(xCenter, 0.0f, 1.0f),
                                        MathUtils.clamp(yCenter, 0.0f, 1.0f),
                                        MathUtils.clamp(w, 0.0f, 1.0f),
                                        MathUtils.clamp(h, 0.0f, 1.0f)
                                }
                        );
                    }
                }
            }
        }
    }

    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public float getIouThreshold() {
        return iouThreshold;
    }

    public void setIouThreshold(float iouThreshold) {
        this.iouThreshold = iouThreshold;
    }
}
