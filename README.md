## Conception of a shared file system

This is a shared file system in command line written in java.

### Specifications

- files are stored in servers memory
- all servers can crash
- when a server crashes, he loses all his files
- only K servers can srash simultanously
- crashes are far beetween so the system can synchronize

### Install

For now you can only launch the filesystem locally. 

Compile the sources (with netbeans 7.4). Run `./deploy.sh`. Go to `~/shared_filesystem/server0/`. Do `java -jar server.java 0`. Repeat it for every server you want to launch.
