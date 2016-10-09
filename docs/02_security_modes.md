# Security

> Author: AstromechZA
> Date: 09 October 2016

This page will attempt to explain and rationalise the security model and the reasons it operates
the way it does. All efforts are taken to keep this project as secure as possible.

----

### TL;DR:

- Files are encrypted by a file-unique random symmetric key that changes each time the file is written
    - can pick between AES 128, AES 256, TWOFISH 128, or TWOFISH 256. All use CTR mode.
- File encryption key is stored in the `inventory` section
- `inventory` section is encrypted using a symmetric key derived from a key derivation function
- Derivation function can be either PBKDF2 or SCrypt. Calculation cost can be specified.

All encryption settings can be tweaked and we provide multiple different algorithms with customisable
strength so that in the future if an algorithm is shown to be broken, users can re-encrypt with better
security settings or an alternative non-broken cipher without waiting for an update of this software.

