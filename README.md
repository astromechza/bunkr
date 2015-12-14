# bunkr_beta

## Run unit tests

```
$ bundle exec buildr test
```

## Run unit tests with coverage:

```
$ bundle exec buildr test jacoco:report
```

## Build JAR

```
$ bundle exec buildr package
```

## For setting up your environment

```
$ bundle exec buildr idea
$ bundle exec buildr bunkr_beta:pulllibs
```

## To produce a compacted release version

Download and extract Proguard: http://proguard.sourceforge.net/

Then package it up and run it through proguard to compact it.

```
$ bundle exec buildr package
$ ./<proguard directory>/bin/proguard.sh @bunkr.pro
```
