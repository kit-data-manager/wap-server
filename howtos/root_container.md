---
absorb: true
---

Whenever the application is first started, the database is created using
a root container IRI that is determined from the configuration found at this time. It cannot be changed afterwards
because the root container IRI is a prefix to all stored elements IRIs. When it changes, they all would have to change.

The only way to get the application to work again is then to completely remove the database and have it recreated.
No access to any data created before exists then anymore.

The root container IRI is created using the following parameters:
- Hostname
- EnableHttps
- WapPort

Example 1: Hostname=localhost, EnableHttps=false, WapPort=8080
===> root IRI = http://localhost:8080/wap/

Example 2: Hostname=example.org, EnableHttps=false, WapPort=80
===> root IRI = http://example.org/wap/   (port is omitted because it is the default for HTTP)

Example 3: Hostname=localhost, EnableHttps=true, WapPort=1443
===> root IRI = https://localhost:1443/wap/

Example 4: Hostname=host1.example.org, EnableHttps=true, WapPort=443
===> root IRI = https://host1.example.org/wap/   (port is omitted because it is the default for HTTPS)

When using the installer (via --install or by starting the jar in an empty folder) it asks for this base configuration
and shows its consequences on the root container IRI.

All of the above can be overwritten by using the `WapBaseUrl` property.
This property is intended to be used only in scenarios where the server is reachable from a different URL
and if this should or must be reflected in the database (reverse proxy, docker container).

If changing any of those parameters with an already running server is necessary, a deletion of the database is needed.
It gets recreated on first startup after the configuration has been changed.

---

**NOTE**
Using manual database manipulation, a conversion of the database to fit the new root container IRI can be achieved,
but this is not implemented in the application. The easiest way to achieve this would be to have the database
been backed up to NQUADS (which retains the named graphs) and then run a simple text replacement of old IRI ==> new IRI.
Also see [data migration guide](https://github.com/kit-data-manager/wap-server/wiki/Migrating-a-Server-Instance-and-its-Data) for details.

---