# Bunkr

See [DEMO.md](DEMO.md) for usage examples.

## Development

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

See [jarsquasher tool](tools/jarsquasher/README.md)

### To regenerate the DEMO.md from a packages jar

See [clidocgen tool](tools/clidocgen/README.md)
