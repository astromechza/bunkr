# Bunkr

## 1. Development

### Run unit tests

```
$ bundle exec buildr test
```

### Run unit tests with coverage:

```
$ bundle exec buildr test jacoco:report
```

### Build JAR

```
$ bundle exec buildr package
```

### For setting up your environment

```
$ bundle exec buildr idea
$ bundle exec buildr bunkr:pulllibs
```

### To produce a compacted release version

Download and extract Proguard: http://proguard.sourceforge.net/

Then package it up and run it through proguard to compact it.

```
$ bundle exec buildr package
$ ./<proguard directory>/bin/proguard.sh @bunkr.pro
```

## 2. Usage

Just a little dump of the help page for now.

```
usage: bunkr [-h] [--version] [-p PASSWORD-FILE] archive {auth,create,find,hash,ls,mkdir,mv,read,rm,tags,write} ...

positional arguments:
  archive                path to the archive file
  {auth,create,find,hash,ls,mkdir,mv,read,rm,tags,write}
    auth                 check for password authentication to open the archive
    create               create a new empty archive
    find                 search for files or folders by name or tags
    hash                 calculate a integrity hash for a file in the archive
    ls                   list the contents of a folder
    mkdir                construct a directory
    mv                   move a file or folder to a different path
    read                 read or export a file from the archive
    rm                   remove a file or directory
    tags                 list, set or add tags to a file
    write                write or import a file

optional arguments:
  -h, --help             show this help message and exit
  --version
  -p PASSWORD-FILE, --password-file PASSWORD-FILE
                         read the archive password from the given file
```
