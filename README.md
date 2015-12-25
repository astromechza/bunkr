# Bunkr

See [DEMO.md](DEMO.md) for usage examples.

## Development

The project has been organised into 3 parts:

- ```bunkr-core``` the core functionality behind the archive file format
- ```bunkr-cli``` a command line interface for creating and managing archives
- ```bunkr-gui``` a graphical interface for browsing and managing content of archives (this is the focus of this project once the core and cli are stable).

This project uses Apache Buildr for managing compilation and packaging. Each part can be compiled, tested, and packaged individually.


#### Examples

```
# build
$ bundle exec buildr bunkr:bunkr-core:compile
# test
$ bundle exec buildr bunkr:bunkr-core:test
# run unit tests with coverage analyser
$ bundle exec buildr bunkr:bunkr-core:test jacoco:report
# package into a jar
$ bundle exec buildr bunkr:bunkr-core:package
```

#### For setting up your IntelliJ environment

```
$ bundle exec buildr idea
$ bundle exec buildr bunkr:pulllibs
```

### To produce a compacted release version

See [jarsquasher tool](tools/jarsquasher/README.md)

### To regenerate the DEMO.md from a packages jar

See [clidocgen tool](tools/clidocgen/README.md)
