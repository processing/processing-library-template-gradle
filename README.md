# Deep Vision Processing [![Build Status](https://travis-ci.org/cansik/deep-vision-processing.svg?branch=master)](https://travis-ci.org/cansik/deep-vision-processing)
Deep computer-vision algorithms for [Processing](https://processing.org/).

The idea behind this library is to provide a simple way to use (inference) neural networks for computer vision tasks inside Processing. Mainly portability and easy-to-use are the primary goals of this library. Because of that (and [javacv](https://github.com/bytedeco/javacpp-presets/pull/832)), **no GPU support** at the moment.

_Caution_: The API is still in development and can change at any time.

![Pose](readme/pose.jpg)

*Lightweight OpenPose Example*

## Install
Download the [latest](releases/download/0.3.4/deepvision.zip) prebuilt version from the [release](releases) sections and install it into your Processing library folder.

Because the library is still under development, it is not yet published in the Processing contribution manager.

## Usage
The base of the library is the `DeepVision` class. It is used to download the pretrained models and create new networks.

```java
import ch.bildspur.vision.*;
import ch.bildspur.vision.network.*;
import ch.bildspur.vision.result.*;

DeepVision vision = new DeepVision(this);
```

Usually it makes sense to define the network globally for your sketch and create it in setup. The `create` method downloads the pre-trained weights if they are not already existing. The network first has to be created and then be setup.

```java
YOLONetwork network;

void setup() {
  // create the network & download the pre-trained models
  network = vision.createYOLOv3();

  // load the model
  network.setup();
  
  // set network settings (optional)
  network.setConfidenceThreshold(0.2);
  
  ...
}
```

By default, the weights are stored in the library folder of Processing. If you want to download them to the sketch folder, use the following command:

```java
// download to library folder
vision.storeNetworksGlobal();

// download to sketch/networks
vision.storeNetworksInSketch();
```

Each network has a `run()` method, which takes an image as a parameter and outputs a result. You can just throw in any PImage and the library starts processing it.

```java
PImage myImg = loadImage("hello.jpg");
ArrayList<ObjectDetectionResult> detections = network.run(myImg);
```

Please have a look at the specific networks for further information or at the [examples](examples).

## Networks

Here you find a list of implemented networks:

- Object Detection ✨
	- YOLOv3-tiny
	- YOLOv3-spp ([spatial pyramid pooling](https://stackoverflow.com/a/55014630/1138326))
	- YOLOv3 (608)
	- SSDMobileNetV2
	- Ultra-Light-Fast-Generic-Face-Detector-1MB RFB (~30 FPS on CPU)
	- Ultra-Light-Fast-Generic-Face-Detector-1MB Slim (~40 FPS on CPU)
	- Handtracker based on SSDMobileNetV2
	- TextBoxes
- Object Recognition 🚙
    - Tesseract LSTM
- Keypoint Detection 🤾🏻‍♀️
	- Facial Landmark Detection
	- Single Human Pose Detection based on lightweight openpose
- Classification 🐈
    - MNIST CNN
    - FER+ Emotion
    - Age Net
    - Gender Net

The following list shows the networks that are on the list to be implemented:

* YOLO 9K (not supported by OpenCV)
* Multi Human Pose Detection (currently struggling with the partial affinity fields 🤷🏻‍♂️ help?)
* MaskRCNN
* TextBoxes++
* [CRNN](https://github.com/bgshih/crnn)
* [PixelLink](https://github.com/ZJULearning/pixel_link)


### Object Detection

#### YOLOv3

#### SSDMobileNetV2

#### Ultra-Light-Fast-Generic-Face-Detector

#### Handtracker

#### TextBoxes

### Object Recognition
tbd

### KeyPoint Detection
tbd

### Classification
tbd

## Build
- Install JDK 8 (because of Processing)
- Download maven snapshots for JavaCV: `mvn -U compile`

Run gradle to build a fat jar:

```bash
# windows
gradlew.bat fatjar

# mac / unix
./gradlew fatjar
```

Create a new release:

```bash
./release.sh version
```

## FAQ

> Why is xy network not implemented?

Please open an issue if you have a cool network that could be implemented or just contribute a PR.

> Why is it no possible to train my own network?

The idea was to give artist and makers a simple tool to run networks inside of Processing. To train a network needs a lot of specific knowledge about Neural Networks (CNN in specific).

Of course it is possible to train your own YOLO or SSDMobileNet and use the weights with this library.

> Is it compatible with Greg Borensteins [OpenCV for Processing](https://github.com/atduskgreg/opencv-processing)?

No, OpenCV for Processing uses the direct OpenCV Java bindings instead of JavaCV. Please only include either one library, because Processing gets confused if two OpenCV packages are imported.

## About
Maintained by [cansik](https://github.com/cansik) with the help of the following dependencies:

- [bytedeco/javacv](https://github.com/bytedeco/javacv)
- [atduskgreg/opencv-processing](https://github.com/atduskgreg/opencv-processing)

Stock images from the following peoples have been used:

- yoga.jpg by Yogendra Singh from Pexels
- office.jpg by [fauxels](https://www.pexels.com/@fauxels) from Pexels
- faces.png by [shvetsa](https://www.pexels.com/@shvetsa) from Pexels
- hand.jpg by Thought Catalog on Unsplash
- sport.jpg by John Torcasio on Unsplash
- sticker.jpg by 🇨🇭 Claudio Schwarz | @purzlbaum on Unsplash
