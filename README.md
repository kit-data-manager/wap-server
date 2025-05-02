# Web Annotation Protocol Server

![Travis (.com)](https://img.shields.io/travis/com/kit-data-manager/wap-server)
![Code Coverage](https://img.shields.io/coveralls/github/kit-data-manager/wap-server.svg)
![License](https://img.shields.io/github/license/kit-data-manager/wap-server.svg)

This project contains a server for creating and managing annotations based on the Web Annotation Data Model (WADM) implementing
the complete Web Annotation Protocol (WAP). The service is realized as microservice using Spring Boot and can be operated standalone.


## How to build

To install the application from source, see howtos/INSTALL. This way no IDE is needed.

To open this project in Eclipse please follow these steps: (on other IDEs, the steps might be different)

The STS (Spring Tool Suite) plug-in should be installed

- generate a new java project
- connect it to git and pull the source code
- right click on the Project and choose "Spring Tools" -> "Add Spring Project Nature"
- right click on the Project and choose "Spring Tools" -> "Update Maven Dependencies"
- right click again an choose "Configure" -> "Convert to Maven Project"
- The src folder src/main/installer/java may not have been added automatically.
  Add it through right-click on it, "Build Path" ==> "Use as Source Folder".  

Now it should be possible to right click on the project and choose "Run as" -> "Spring Boot App"

## More Information

* [Web Annotation Data Model](https://www.w3.org/TR/annotation-model/)
* [Web Annotation Protocol](https://www.w3.org/TR/annotation-protocol/)

## License

The WAP Server is licensed under the Apache License, Version 2.0.
