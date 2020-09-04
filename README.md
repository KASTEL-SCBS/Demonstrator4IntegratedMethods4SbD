# Demonstrator4IntegratedMethods4SbD
## Privacy Crash Cam 
### Code-Repositories
* https://github.com/cCirclEe/pcc-imp-app
* https://github.com/cCirclEe/pcc-imp-webservice
* https://github.com/cCirclEe/pcc-imp-webinterface
### Dokumentation
* https://github.com/cCirclEe/pcc-funcspec
* https://github.com/cCirclEe/pcc-design
* https://github.com/cCirclEe/pcc-imp
* https://github.com/cCirclEe/pcc-qa
## Goal-Modeling-Editor
* https://github.com/Baakel/KastelEditor
## Component-Based Software Engineering for KASTEL 
* https://github.com/KASTEL-SCBS

Build
-----
```sh
  ./setup_deps
```

PCC Web
-------
Setup the postgres database using the `setup_db` script.

Start both the webinterface and webservice (see the respective READMEs or
run the `build_web` script to build and the `run_web` script to run both
applications).

A test user is available with
```
	username: test3@example.de
	password: 123456
```
Videos for this user were manually added and are therefore not anonymized.

Other users are available
(`test@example.de` up to `test12@example.de`, same password as above).
However, the metadata and the videos were not added for the remaining users,
so the server will complain.
