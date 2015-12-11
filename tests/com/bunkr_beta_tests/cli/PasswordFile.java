package com.bunkr_beta_tests.cli;

import com.bunkr_beta.RandomMaker;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 */
public class PasswordFile
{
    public static File genPasswordFile(File dest, byte[] content) throws IOException
    {
        if (content == null) content = DatatypeConverter.printHexBinary(RandomMaker.get(64)).getBytes();
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest)))
        {
            bos.write(content);
            bos.write('\n');
        }
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(dest.toPath(), permissions);
        return dest;
    }

    public static File genPasswordFile(File dest) throws IOException
    {
        return genPasswordFile(dest, null);
    }
}
