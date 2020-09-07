![Travis (.com)](https://img.shields.io/travis/com/kit-data-manager/wap-server)
![Code Coverage](https://img.shields.io/coveralls/github/kit-data-manager/wap-server.svg)
![License](https://img.shields.io/github/license/kit-data-manager/wap-server.svg)

# Web Annotation Protocol Server

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
