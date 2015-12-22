# Bunkr CLI Usage Examples
This document shows the current status of the CLI and some of the commands you can use.

## 1. Print the version of Bunkr you are running
```bash
$ bunkr --version
--------------- <output> ---------------
version: 0.3.0
commit date: 2015-12-23T00:08:51+02:00
commit hash: f714861e7c680e446fcfcda680f18b4ace9574dd
```

## 2. Print the help information
```bash
$ bunkr --help
--------------- <output> ---------------
usage: bunkr [-h] [--version] [-p PASSWORD-FILE] archive {change-password,check-password,create,find,hash,ls,mkdir,mv,read,rm,tags,write} ...

positional arguments:
  archive                path to the archive file
  {change-password,check-password,create,find,hash,ls,mkdir,mv,read,rm,tags,write}
    change-password      change the password protecting the archive
    check-password       test a password or password file against the archive
    create               create a new empty archive
    find                 search for files or folders by name or tags
    hash                 calculate a hash over the contents of a file in the archive
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

## 3. Create a new archive
```bash
$ echo DemoPassword > .password && chmod 600 .password
```

```bash
$ bunkr demo.bunkr --password-file .password create
--------------- <output> ---------------
Created new archive demo.bunkr
```

## 4. Add some content from a file
```bash
$ echo 'The quick brown fox jumped over the lazy dog' > file1.txt
```

```bash
$ bunkr demo.bunkr -p .password write /file_one file1.txt -t demo-tag
--------------- <output> ---------------
Importing file: |==============================================================================|Importing file: |==============================================================================|
```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE  MODIFIED      NAME      TAGS      
45B   Dec 23 00:11  file_one  demo-tag
```

## 5. Check integrity
```bash
$ bunkr demo.bunkr -p .password hash /file_one -a md5
--------------- <output> ---------------
Calculating hash: |==============================================================================|Calculating hash: |==============================================================================|
36dc7e16fee91d13c807a356177ee404
```

```bash
$ md5 file1.txt
--------------- <output> ---------------
MD5 (file1.txt) = 36dc7e16fee91d13c807a356177ee404
```

## 6. Add some folders and another file
```bash
$ head -c 514229 /dev/urandom  > file2.txt
```

```bash
$ bunkr demo.bunkr -p .password mkdir /sample/dir -r
```

```bash
$ bunkr demo.bunkr -p .password write /sample/dir/file_two file2.txt
--------------- <output> ---------------
Importing file: |==============================================================================|Importing file: |==============================================================================|
```

Show everything in the tree so far:
```bash
$ bunkr demo.bunkr -p .password find /
--------------- <output> ---------------
/file_one
/sample/
/sample/dir/
/sample/dir/file_two
```

See how the file reflects the size in bytes
```bash
$ bunkr demo.bunkr -p .password ls /sample/dir/ --machine-readable
--------------- <output> ---------------
SIZE    MODIFIED                  NAME      TAGS  
514229  2015-12-23T00:11:22+0200  file_two
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  517093 Dec 23 00:11 demo.bunkr
```

/dev/urandom didn't compress very well, so the compression offered by Bunkr hasn't really helped :(."

## 7. Lets try something more compressible
```bash
$ for i in {1..400}; do echo 'Lorem ipsum dolor sit amet' >> file3.txt; done; ls -al file3.txt
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  10800 Dec 23 00:11 file3.txt
```

```bash
$ bunkr demo.bunkr -p .password write /file_three file3.txt
```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE   MODIFIED      NAME        TAGS      
                     sample/     
45B    Dec 23 00:11  file_one    demo-tag  
10.5K  Dec 23 00:11  file_three
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  518437 Dec 23 00:11 demo.bunkr
```

That compressed much better. It only added 1024 bytes to the size. Files are managed using blocks of 1024 bytes, so that is the minimum size on disk.

*Autogenerated with ```bunkr-0.3.0.jar``` at ```2015-12-23T00:08:51+02:00 f714861e7```*
