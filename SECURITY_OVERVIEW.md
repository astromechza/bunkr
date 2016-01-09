# Security Overview & File Format

This document outlines the methods Bunkr uses to keep files secure and how the underlying storage system works.
The document is written based on the v0.6.0 release and may be out of date compared to the current master branch.

All crypto is done using the Bouncy Castle API.

## File format

On disk, a Bunkr archive is a sequence of different data sections:

```
+--------+------------+------------+-----------+
| HEADER | DATABLOCKS | DESCRIPTOR | INVENTORY |
+--------+------------+------------+-----------+
```

The sections are described in more depth below.

### HEADER

The header contains essential information regarding the format of the file.

- Format Header "BUNKR"
- Version Number of Bunkr used to create the archive
- Block size used for DATABLOCKS section
- Length of the DATABLOCKS section

The DATABLOCKS block size and length can be seen as belonging to the DATABLOCKS section.

### DATABLOCKS

This section is a sequence of byte blocks, each one `Block size` bytes long. There may be 0 data blocks if there are no
non-empty files in the archive. There can be up to 2^32 blocks.

### DESCRIPTOR

The descriptor is the next part of the file format, it holds the information required to read the inventory section. At
the time of writing 2 different descriptors can be used:

- Plaintext: indicating that the INVENTORY section is not encrypted.
- PBKDF2: indicating that the INVENTORY section requires a password to decrypt. Additional parameters are stored,
declaring the AES key length, the number of rounds used by the PBKDF2 password derivation algorithm, and the salt used
for PBKDF2. The AES key length defaults to 256, the PBKDF2 iterations defaults to 10,000, and the salt is 128 bits of
random. At the time of writing, the salt is not refreshed on each encryption, but there is an issue for that: #24.

*Note:* We will be adding an SCrypt descriptor and possibly an Argon2 descriptor when it is available. Follow issue #25
for this.

### INVENTORY

This section is a JSON blob that contains the file structure. It has 2 booleans that indicate whether the files
stored in the archive are compressed and whether they are encrypted. Each file in the structure contains all of its
metadata like file names, sizes, encryption keys, and a range of blocks in the DATABLOCKS section.

The inventory json is encrypted in the manner described by the descriptor.

At the time of writing, files are compressed using Deflate, and files are encrypted using 256 AES in SIC (CTR) mode.
