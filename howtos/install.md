---
absorb: true
---

---
**NOTE**
Please make sure, that the folder where you want to run the WAP Server from is empty. Otherwise, the installation procedure 
automatically performed on first startup won't work and has to be started manually by providing the argument *--install*
 in the next step.

---

In order to start the service, change to the service folder and call:

```
user@localhost:/home/user/wap-server$ cd /home/user/wap-instance
user@localhost:/home/user/wap-server$ java -jar wap-server.jar
Part found : wap-server.jar!
Part found : wap-server.jar!
#################################
### Starting installation
#################################
[...]
```

This will guide you through the installation of the service where you can initially configure your server. At the end of the process
you can either directly start the server or end the installation to adapt certain configuration properties, which can be found in the
file *application.properties*, which was created by the installation procedure.
