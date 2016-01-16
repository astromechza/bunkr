  # Bunkr CLI Usage Examples
  This document shows the current status of the CLI and some of the commands you can use.

## 1. Print the version of Bunkr you are running
```bash
$ bunkr --version
--------------- <output> ---------------
bunkr-cli-0.8.0.jar
version: 0.8.0
commit date: 2016-01-16T20:26:40+02:00
commit hash: 6bcf58ee2acc248172a174b18c62c01284f3a5b9
```

## 2. Print the help information
```bash
$ bunkr --help
--------------- <output> ---------------
usage: bunkr [-h] [--version] [--logging] [-p PASSWORDFILE] archive {change-security,check-password,create,find,hash,ls,mkdir,mv,read,rm,write}
             ...

positional arguments:
  archive                path to the archive file
  {change-security,check-password,create,find,hash,ls,mkdir,mv,read,rm,write}
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
$ bunkr demo.bunkr change-security scrypt .password
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
$ bunkr demo.bunkr -p .password write /file_one file1.txt --no-progress
--------------- <output> ---------------

```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE  MODIFIED      NAME      
45B   Jan 16 20:42  file_one
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
SIZE    MODIFIED                  NAME      
514229  2016-01-16T20:42:52+0200  file_two
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  517213 Jan 16 20:42 demo.bunkr
```

  /dev/urandom didn't compress very well, so the compression offered by Bunkr hasn't really helped :(.

## 7. Lets try something more compressible
```bash
$ for i in {1..400}; do echo 'Lorem ipsum dolor sit amet' >> file3.txt; done; ls -al file3.txt
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  10800 Jan 16 20:42 file3.txt
```

```bash
$ bunkr demo.bunkr -p .password write /file_three file3.txt
```

```bash
$ bunkr demo.bunkr -p .password ls /
--------------- <output> ---------------
SIZE    MODIFIED      NAME        
                      sample/     
45B     Jan 16 20:42  file_one    
10.5Ki  Jan 16 20:42  file_three
```

```bash
$ ls -al demo.bunkr
--------------- <output> ---------------
-rw-r--r--  1 benmeier  staff  518604 Jan 16 20:42 demo.bunkr
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

*Autogenerated with ```bunkr-cli-0.8.0.jar``` at ```2016-01-16T20:26:40+02:00 6bcf58ee2```*
