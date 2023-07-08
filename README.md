# Gradle/IntelliJ template for Processing library 

The following describes how to set up a Processing Library project in [Gradle](https://gradle.org/), build it successfully, use [IntelliJ IDEA](https://www.jetbrains.com/idea/) for development/debugging of the Library, and to make your Library ready for distribution.

## Build
- First, install [Adoptium OpenJDK 17](https://adoptium.net/) (required by Processing 4+)

Run the included gradle wrapper command to compile the source code:

```bash
# windows
gradlew.bat compileJava

# mac / unix
./gradlew compileJava
```

To build a new release package under `/release/YourLibrary.zip`, use:

```bash
# windows
gradlew.bat releaseProcessingLib

# mac / unix
./gradlew releaseProcessingLib
```

If you have Gradle installed in your sustem, you can replace ```gradlew``` with ```gradle``` in the commands above.

## Developing in IntelliJ IDEA

The library can be imported as an IntelliJ project following the steps below:

- Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
- Clone this repo and build the library following the instructions in the previous section
- Clone the [processing4 repo](https://github.com/processing/processing4)
- Create new project in IntelliJ with the name and location of your choice, for example ```lib-dev```
- Create new module in the project for core Processing, using as content root and the module file location the ```core``` folder under the processing4 repo. As "JARs or Directory" dependency, add ```<path to processing4 repo>/core/library```
- Create another module in the project, this time for YourLibrary. Use the YourLibrary's root folder as the content root and module file location. Add the processing-core module as module dependency for this module, and the ```libs``` subdirectory inside the YourLibrary directory (it should have been created during library building step) as its "JARs or Directory" dependency
- Add the proccessing-core and YourLibrary modules as dependencies in the main module of the project (```lib-dev```)
- You can now create a test program in under the main module of the project:

```
import processing.core.*;

public class HelloTest extends PApplet {

    public void settings() {
        size(parseInt(args[0]), parseInt(args[1]));
        smooth();
    }

    public void setup() {
        hello = new HelloLibrary(this);
  
        PFont font = createFont("Arial", 40);
        textFont(font);
    }

    public void draw() {
        background(0);
        fill(255);
        text(hello.sayHello(), 40, 200);
    }

    static public void main(String[] args) {
        PApplet.main(DetectTest.class, "400", "400");
    }
}
```

Please note that any file read from the IntelliJ program should be placed inside the subdirectory ```data``` located inside the root of the IntelliJ project (i.e.: ```lib-dev/data```)

## Contributors

This template is based on the [Deep Vision Library](https://github.com/cansik/deep-vision-processing) by [Florian Bruggisser](https://github.com/cansik), and was created as part of [Jeongin Lee](https://github.com/jjeongin)'s [Machine Learning Library](https://github.com/jjeongin/ml4processing) project during during Google Summer of Code 2022.