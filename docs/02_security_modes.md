# Security

> Author: AstromechZA
> Date: 09 October 2016

This page will attempt to explain and rationalise the security model and the reasons it operates
the way it does. All efforts are taken to keep this project as secure as possible.

----

### TL;DR:

- Files are encrypted by a file-unique random symmetric key that changes each time the file is written
    - can pick between AES 128, AES 256, Twofish 128, or Twofish 256. All use CTR mode.
- File encryption key is stored in the `inventory` section
- `inventory` section is encrypted using a symmetric key derived from a key derivation function
- Derivation function can be either PBKDF2 or SCrypt. Calculation cost can be specified.

All encryption settings can be tweaked and we provide multiple different algorithms with customisable
strength so that in the future if an algorithm is shown to be broken, users can re-encrypt with better
security settings or an alternative non-broken cipher without waiting for an update of this software.

### File encryption

The datablocks are stored in an encrypted and compressed format. Files are compressed before encryption.

We use symmetric encryption here for better read/write peformance compared to assymetric ciphers like RSA
which are much slower and can only securely encrypt small amounts of data.

The cipher suites supported are:

#### 1. AES (either 128bit or 256bit key length)
    See [wikipedia](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) for more info. AES is a secure
    symmetric block cipher approved by NIST and other encryption comittees. We support both 128 and 256
    bit key lengths although even 128bit keys provide sufficient security for the forseable future.

#### 2. Twofish (either 128bit or 256bit key length)
    See [wikipedia](https://en.wikipedia.org/wiki/Twofish) for more info. Twofish is another symmetric block
    cipher that was proposed as an alternative during the AES selection process. The Twofish algorithm is not
    patented or restricted in any way. We hope it is sufficiently different to AES in order to provide an alternative
    if AES is seen to be backdoored, or broken.

Both ciphers are used in CTR mode in order to operate as streaming ciphers on-top of the underlying compression
stream. We use an integrity hash to provide some of the benefits of authenticated encryption.

Each time a file is written or rewritten a new `key` and `iv` is chosen for it. This key and iv are both filled
with bytes from the Java `SecureRandom` random generator.

If the data in a block is smaller than the block size, the remainder of the block is filled with random data. This
hopefully makes it impossible to approximate the size of the file with more accuracy than simply the number of 1024
byte blocks it has used in the archive. In fact, with the inventory section encrypted, the datablocks section looks
completely random with no distinction between used blocks, blank blocks, or which blocks are used by which files.


### Inventory encryption

The inventory contains the file metadata including the encryption algorithm and symmetric keys for each file. This means
we definitely must encrypt this if encryption is enabled.

The inventory section is encrypted by a padded block cipher using either AES128, AES256, Twofish128, or Twofish256 with
PKCS7 padding.

The key and iv for the chosen cipher are calculated using a key derivation algorithm. We support the following algorithms:

#### 1. PBKDF2
    See [wikipedia](https://en.wikipedia.org/wiki/PBKDF2) for more info. pbkdf2 is a time-intensive algorithm that takes
    the input password and combines it
    with a salt before peforming many many iterations of a SHA256 hash in order to consume time. The user can pick how
    much time this should take and we perform iterations of SHA256 on the current hardware in order to calculate the number
    of iterations. We store the unique salt and number of iterations in the `descriptor` section. At the moment the user
    can choose anything from 100ms to 10 seconds. This impact is hit whenever the inventory section is written or read.

#### 2. Scrypt
    See [wikipedia](https://en.wikipedia.org/wiki/Scrypt) for more info. Scrypt is a memory-intensive algorithm that takes
    that can be configured to require an amount of RAM required to calculate the result. Again, the user can pick this
    memory requirement. At the moment the user can choose anything from 16 MiB to 1 GiB.

We provide both of these key derivation algorithms so that the user can pick it depending on the hardware they use most
commonly. The user can change and upgrade this encryption strength at any time.


### Changing encryption algorithms

The user is able to change the encryption settings of the archive whenever they want. The encryption of the inventory
can be changed without re-necryption of each file. Even when the encryption cipher is changed the user is not forced to
re-encrypt files, since this may take a long time if the files are large. Re-encrypting files according to the new
settings is done asynchronously by the user.


### Re-encrypting files

Files can be reencrypted whenever the user requires it. This will change the keybytes and renecrypts the entire file.
