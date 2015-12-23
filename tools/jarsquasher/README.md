# Jar Squasher

This folder contains a single Ruby script that can be used to run Proguard Jar optimiser to build a final release
version.

## Getting Proguard

Download from ```http://proguard.sourceforge.net/```. Unzip it somewhere accessible. We're interested in the proguard.sh
file in its ```bin/``` directory.

## How to run

```
ruby ./tools/jarsquasher/jarquasher.rb ./target/bunkr-X.X.X.jar ~/Downloads/proguard5.2.1/bin/proguard.sh
```
