package org.bunkr_tests.cli;

import org.bunkr.RandomMaker;

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
        String password = DatatypeConverter.printBase64Binary(RandomMaker.get(100));
        if (content == null) content = password.getBytes();
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest)))
        {
            bos.write(content);
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
