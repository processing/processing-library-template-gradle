> [!WARNING]
> This repository is deprecated. Please use the latest Gradle template at [processing/processing-library-template](https://github.com/processing/processing-library-template).

# Gradle/IntelliJ template for Processing library 

The following describes how to set up a Processing library project in [Gradle](https://gradle.org/), build it successfully, use [IntelliJ IDEA](https://www.jetbrains.com/idea/) for development/debugging of the library, and to make your library ready for distribution.

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

If you have Gradle installed in your system, you can replace ```gradlew``` with ```gradle``` in the commands above.

## Developing in IntelliJ IDEA

The library can be imported as an IntelliJ project following the steps below:

- Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/).
- Clone this repo and build the library following the instructions in the previous section.
- Clone the [processing4 repo](https://github.com/processing/processing4).
- Create a new project in IntelliJ with the name and location of your choice, for example ```lib-dev```. This project is where you can create new Processing examples to test your library.
- Create a new module in the project to import the core of Processing, for example in a name ```processing-core```, using the ```core``` folder under the processing4 repo as the content root and module file location. As "JARs or Directory" dependency of this module, add ```<path to processing4 repo>/core/library```. Make sure to use IntelliJ as the Build System.
- Create another module in the project, this time for ```YourLibrary```. This is the Processing library project you have been working on, in this case, this repository. Use the root folder of ```YourLibrary```, or ```processing-library-template-gradle```, as the content root and module file location. Add the ```processing-core``` module as a module dependency for this module.
- Add the ```processing-core``` and ```YourLibrary``` modules as module dependencies to the main module of the project (```lib-dev```). 
- Add the ```libs``` subdirectory inside the ```YourLibrary``` directory (it should have been created during the library building step in the above section) as a "JARs or Directory" dependency of the main module, ```lib-dev```.
- You can now create a test program under the ```src``` folder of the main module of the ```lib-dev``` project:

```
import processing.core.*; // import processing core
import template.library.*; // import sample library

public class HelloTest extends PApplet {
    HelloLibrary hello;

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
        PApplet.main(HelloTest.class, "400", "400");
    }
}
```

You should be able to run the program from the IntelliJ IDE.

Please note that any file read from the IntelliJ program should be placed inside the subdirectory ```data``` located inside the root of the IntelliJ project (i.e.: ```lib-dev/data```)

## Contributors

This template is based on the [Deep Vision Library](https://github.com/cansik/deep-vision-processing) by [Florian Bruggisser](https://github.com/cansik), and was created as part of [Jeongin Lee](https://github.com/jjeongin)'s [Machine Learning Library](https://github.com/jjeongin/ml4processing) project during Google Summer of Code 2022, mentored by [Andrés Colubri](https://github.com/codeanticode).
