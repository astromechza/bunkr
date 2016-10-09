# Overview of Bunkr

> Author: AstromechZA
> Date: 09 October 2016

Bunkr is a potentially-secure encrypted file store. It serves to store an arbitrary virtual file structure in a
compressed and encrypted manner while allowing read and write access to these files once inside the archive.

Bunkr provides two different interfaces:

1. a CLI interface designed for basic terminal actions and for use by scripts and other applications
2. a GUI interface which provides the most useful functionality for users in a desktop environment

This documentation aims to explain various areas of the project in an attempt to explain why one could consider it
secure and what security is offered.

Bear in mind that at this point it is designed for power users who understand various crytographical concepts and
conventions.

### Contents:

- [The file format](01_file_format.md)
- [Security modes](02_security_modes.md)
