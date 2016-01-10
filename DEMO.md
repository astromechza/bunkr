# Bunkr CLI Usage Examples
This document shows the current status of the CLI and some of the commands you can use.

## 1. Print the version of Bunkr you are running
```bash
$ bunkr --version
--------------- <output> ---------------
bunkr-cli-0.7.0.jar
version: 0.7.0
commit date: 2016-01-10T22:35:21+02:00
commit hash: 83a1de5540af59956135f14d394c94e2e2377257
```

## 2. Print the help information
```bash
$ bunkr --help
--------------- <output> ---------------
usage: bunkr [-h] [--version] [--logging] [-p PASSWORDFILE] archive
             {change-security,check-password,create,find,hash,ls,mkdir,mv,read,rm,tags,write} ...

positional arguments:
  archive                path to the archive file
  {change-security,check-password,create,find,hash,ls,mkdir,mv,read,rm,tags,write}
    change-security      change the security settings for the archive
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
  --logging              Enable debug logging. This may be a security issue due to information leakage.
  -p PASSWORDFILE, --password-file PASSWORDFILE
                         read the archive password from the given file
```

## 3. Create a new archive
```bash
$ bunkr demo.bunkr create
--------------- <output> ---------------
Created new archive demo.bunkr
```

```bash
$ echo DemoPassword > .password && chmod 600 .password
```

```bash
$ bunkr demo.bunkr change-security scrypt .password aes256_ctr
--------------- <output> ---------------
Successfully changed security settings for achive.
Before: Archive Security: plaintext, File Security: NONE
After: Archive Security: scrypt, File Security: AES256_CTR
```

## 4. Add some content from a file
```bash
$ echo 'The quick brown fox jumped over the lazy dog' > file1.txt
```

```bash
$ bunkr demo.bunkr -p .password write /file_one file1.txt -t demo-tag --no-progress
--------------- <output> ---------------

```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE  MODIFIED      NAME      TAGS      
45B   Jan 10 22:49  file_one  demo-tag
```

## 5. Check integrity
```bash
$ bunkr demo.bunkr -p .password hash /file_one -a md5 --no-progress
--------------- <output> ---------------
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
$ bunkr demo.bunkr -p .password write /sample/dir/file_two file2.txt --no-progress
--------------- <output> ---------------

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
514229  2016-01-10T22:49:24+0200  file_two
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  517228 Jan 10 22:49 demo.bunkr
```

/dev/urandom didn't compress very well, so the compression offered by Bunkr hasn't really helped :(."

## 7. Lets try something more compressible
```bash
$ for i in {1..400}; do echo 'Lorem ipsum dolor sit amet' >> file3.txt; done; ls -al file3.txt
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  10800 Jan 10 22:49 file3.txt
```

```bash
$ bunkr demo.bunkr -p .password write /file_three file3.txt
```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE    MODIFIED      NAME        TAGS      
                      sample/     
45B     Jan 10 22:49  file_one    demo-tag  
10.5Ki  Jan 10 22:49  file_three
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  518636 Jan 10 22:49 demo.bunkr
```

That compressed much better. The file of 10800 bytes only added 1024 bytes to the archive size. Files are managed using blocks of 1024 bytes, so that is the minimum effect on disk size.

Now move it to another location
```bash
$ bunkr demo.bunkr -p .password mv /file_three /sample/file_three
```

```bash
$ bunkr demo.bunkr -p .password find / --type file
--------------- <output> ---------------
/file_one
/sample/dir/file_two
/sample/file_three
```

And finally extract it and prove that we didnt lose anything
```bash
$ bunkr demo.bunkr -p .password read /sample/file_three file3-output.txt
```

```bash
$ md5 file3.txt file3-output.txt
--------------- <output> ---------------
MD5 (file3.txt) = 4c8041ccc39a867880da3da1c6a253d2
MD5 (file3-output.txt) = 4c8041ccc39a867880da3da1c6a253d2
```

*Autogenerated with ```bunkr-cli-0.7.0.jar``` at ```2016-01-10T22:35:21+02:00 83a1de554```*
