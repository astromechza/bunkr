# The Bunkr file format

> Author: AstromechZA
> Date: 09 October 2016

**Please Note**: this is only a description of the file format at the time of writing, it should not serve as a guide
for writing applications against the format.

---

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
- Block size used for DATABLOCKS section (default 1024 bytes)
- Length of the DATABLOCKS section in bytes

The DATABLOCKS block size and length can be seen as belonging to the DATABLOCKS section.

### DATABLOCKS

This section is a sequence of byte blocks, each one `Block size` bytes long. There may be 0 data blocks if there are no
non-empty files in the archive. Empty files take up 0 data blocks in this section. There can be up to 2^32 blocks
which with the default block size gives a theoretical maximum of 4TB for the datablocks section. The datablocks for a
particular file may be fragmented in the datablocks section if files have been deleted or expanded. This file
fragmentation is necessary to allow specific files to be read and written without having to move blocks around disk.
Think of it as a primitive block-based file system.

The datablocks are formed by taking the final compressed and encrypted stream and chopping it into `block-size` pieces
and distributing them through the datablocks section. Pieces of a file *always* reserve the entire block which means a
1 byte file will use 1 block in the archive while a 1MB file would use 1024 blocks.

The datablocks used by a particular file are encoded in the `inventory` section of the file format.

### DESCRIPTOR

The descriptor is the next part of the file format, stored as JSON, it holds the information required to read the
inventory section. It specifies the active encryption mode, settings, and any information required for decryption.

The basic structure is:

```
{
    "security": "<string>",
    "params": {
        ...
    }
}
```

### INVENTORY

This section is a JSON blob that contains the file structure. It has 2 booleans that indicate whether the files
stored in the archive are compressed and whether they are encrypted. Each file in the structure contains all of its
metadata like file names, sizes, encryption keys, and a range of blocks in the DATABLOCKS section.

The inventory json is encrypted in the manner described by the descriptor. See the security docs for more information
on this.

The basic plaintext structure is:

```
{
    "files": [
        {
            "name": "myfile.txt"
            "uuid": "eg: c219765a-d656-4ac4-88fc-e184e1ea2ec2"
            "blocks": [...]
            "sizeOnDisk": <size after compression and encryption>
            "actualSize": <real file size>
            "modifiedAt": <time modified or added>
            "encryptionData": <symmetric encryption iv/key for the file>
            "encryptionAlgorithm": <symmetric encryption algorithm identifier>
            "integrityHash": <sha1 hash of written data>
            "mediaType": <string of what kind of file it is>
        },
        ...
    ],
    "folders": [
        {
            "name": "subfolder"
            "uuid": "eg: c219765a-d656-4ac4-88fc-e184e1ea2ec2"
            "files": [
                ...
            ],
            "folders": [
                ...
            ]
        }
    ],
    "defaultEncryptionAlgorithm": "<string>"
}
```

Note that each file has its own symmetric encryption key and its own encryption algorithm setting.
This data is all encrypted by whatever inventory encryption is in place.
