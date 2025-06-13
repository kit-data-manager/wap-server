---
absorb: true
---

It is recommended to use gradle for the build, the maven build will be deprecated in the future. 
Therefore, this guide only describes usage of gradle. 
It is recommended to use the gradle version shipped with the code (gradle wrapper).
In case of specific requirements like a very old java version you may need to use a different gradle versions. These cases are not tested.

## Prerequisites

* git
* Java (jdk 8 - 17)

## Building the WAP Server

At first, you have to clone the software repository and change to the project folder:

```
user@localhost:/home/user/$ git clone https://github.com/kit-data-manager/wap-server.git
[...]
user@localhost:/home/user/$ cd wap-server
user@localhost:/home/user/wap-server$
```

The build can now be started via:

```
user@localhost:/home/user/wap-server$ ./gradlew clean build
[...]
```

For Windows use `gradlew.bat` instead.

Now, copy the final jar file from the *build/libs* folder to an empty folder in order to prepare the first start of the service.

```
user@localhost:/home/user/wap-server$ mkdir /home/user/wap-instance
user@localhost:/home/user/wap-server$ cp build/libs/wap-server.jar /home/user/wap-instance
[...]
```