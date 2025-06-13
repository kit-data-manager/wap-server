---
absorb: true
---

## Prerequisites

* git
* maven installed (tested 3.5.x)
* OpenJDK 8+

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
user@localhost:/home/user/wap-server$ mvn clean verify
[...]
```

Now, copy the final jar file from the *target/* folder to an empty folder in order to prepare the first start of the service.

```
user@localhost:/home/user/wap-server$ mkdir /home/user/wap-instance
user@localhost:/home/user/wap-server$ cp target/PSE-AA-0.0.1-SNAPSHOT.jar /home/user/wap-instance
[...]
```