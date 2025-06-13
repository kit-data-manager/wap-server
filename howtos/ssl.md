---
absorb: true
---

To run the WAP Server with HTTPS, a valid key-store has to exist in a folder *./ssl/* relative to the server's installation folder.

Here are example steps that can be used with openssl to create a self-signed certificate for this purpose.
It has to be adapted to use officially signed certificates. Since these are usually created by provider-dependent tools,
consult the documentation of the registry selected to provide them.

Create key file, enter the key password twice. You are asked for a password, further written as **PrivateKeyPw**.

```
openssl genrsa -aes256 -out wap.key 4096
```

Create certificate file. numerous question getting asked. Either fill them appropriately or just accept defaults.
When asked for common name, you may provide the hostname you have entered before. This is not required.
The argument -days provides the validity in days.

```
openssl req -new -x509 -key wap.key -out wap.crt -days 3650
```

Convert the files to pkcs12 format. You are again asked for a password, further written as **KeyStorePw**.

```
openssl pkcs12 -inkey wap.key -in wap.crt -export -out wap.pkcs12
```

Finally, copy the keystore to *./ssl/* in case you did not execute the previous commands already in this directory.

Within the file *./ssl.conf* created during server installation, replace the values as needed. The default content
of the file is listed below:

```
key-store-file=wap.pkcs12
key-store-password=KeyStorePw
key-password=PrivateKeyPw
server.ssl.alias=1

```

The key alias is 1 if imported without any further options. if it does not fit or has been changed, adapt it.

## Possible Questions

**Question** : I do not know the alias of the key ?
**Answer** : To list the aliases in a store type keytool -list -keystore .\wap.pkcs12

**Question** : I get a "cannot recover the key" Exception ?
**Answer** : There are some posts out there telling that the two passwords need to be the same
to solve this problem. We never had this issue.