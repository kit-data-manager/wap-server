---
absorb: true
---

Regarding CORS, there is only the option to specify the allowed hosts.
To configure this the file denoted in CorsAllowedOriginsPath (default = ./cors_allowed_origins.txt) is used.
If it is not found during application startup, it gets autocreated with the default value of allowing all hosts and has
the following content.

```
#default CORS allowed origins file, * means allow all
#www.example.org ==> allows http://www.example.org and https://www.example.org
*
```

If CORS should be disabled, either remove all lines from the file or comment out everything.
If a * is found, all other lines are ignored (since everything is then allowed anyway).

Only paths should be given and not the protocol like http and https.
Entering allowed.org leads to http://alowed.org and https://allowed.org to be acceptable.

The other parameters are all implemented in a fashion that is either explicitly * or resembles it in the best way.
- All requested methods are allowed (as long as the endpoint allows them in the usual Allow header)
- All headers present in the response are exposed.
- All headers are allowed to be used in requests. The server may reject/ignore the actual requests
  using these headers if the server has no idea what to do with them.

The actual requests then will be answered by an 403 if either CORS is disabled or the origin not allowed.
  
---
**NOTE**
The CORS configuration does not apply to direct SPARQL endpoints. Therefore, it is recommended not to make these endpoints publicly available.

---