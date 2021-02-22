# Registration

This repository contains the two components of registration:
1. Registration Client
1. Registration Processor 

Refer to README in respective folders for details.

### License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)

clone the repository 

```
$ git clone https://github.com/WuriGuinea/wuriguinea-registration.git
```

checkout branch 1.1.1 

```
$ git checkout -t origin/1.1.1
```

go to repository root and run "" ** it will create a zip file registration-client/target

```
$ mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true
```
Extract the zip file into "guinea-client"

copy "MANIFST.MF" to "guinea-client" folder
replace "guinea-client/lib" with registration-client/target/lib
copy "mosip-client" & "mosip-services" from "guinea-client/lib" to "guinea-client/bin"
run run.bat file to launch application
Launch your registration client application (it wont let you login.. thats fine).
Go to home folder, there will be a newly created .mosipkeys folder. Inside .mosipkeys folder, you will find a readme file.
Mail the readme file to system admin to update the machine public keys... Once done you ll be able to run client application
