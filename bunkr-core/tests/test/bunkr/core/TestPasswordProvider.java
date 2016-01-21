/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package test.bunkr.core;

import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.IPasswordPrompter;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created At: 2015-12-02
 */
public class TestPasswordProvider
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testNoPasswordOrPrompt() throws Exception
    {
        PasswordProvider uic = new PasswordProvider();
        assertThat(uic.getPrompter(), is(equalTo(null)));
        uic.clearArchivePassword();
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testNoPrompt() throws Exception
    {
        PasswordProvider uic = new PasswordProvider();
        uic.setArchivePassword("HunterTwo".getBytes());
        assertThat(uic.getHashedArchivePassword(), is(not(equalTo("HunterTwo".getBytes()))));
        assertThat(uic.getPrompter(), is(equalTo(null)));

        uic.clearArchivePassword();
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testWithPrompt() throws Exception
    {
        PasswordProvider uic = new PasswordProvider(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return "AdventureTime".getBytes();
            }
        });

        uic.clearArchivePassword();
        assertThat(uic.getHashedArchivePassword(), is(not(equalTo("AdventureTime".getBytes()))));

        uic.clearArchivePassword();
        uic.setPrompter(null);
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testShortLength() throws Exception
    {
        PasswordProvider uic = new PasswordProvider(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return "bad".getBytes();
            }
        });

        try
        {
            uic.getHashedArchivePassword();
            fail("Should have thrown bad error");
        }
        catch (IllegalPasswordException ignored) {}
    }

    @Test
    public void testBadCharacters() throws Exception
    {
        PasswordProvider uic = new PasswordProvider(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return "badaw\tdoiahwda".getBytes();
            }
        });

        try
        {
            uic.getHashedArchivePassword();
            fail("Should have thrown bad error");
        }
        catch (IllegalPasswordException ignored) {}
    }

    @Test
    public void testBadCharacters2() throws Exception
    {
        PasswordProvider uic = new PasswordProvider(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return ("badaw" + ((char) 0x7F) + "doiahwda").getBytes();
            }
        });

        try
        {
            uic.getHashedArchivePassword();
            fail("Should have thrown bad error");
        }
        catch (IllegalPasswordException ignored) {}
    }

    @Test
    public void testFromFile() throws Exception
    {
        File pwFile = folder.newFile();
        try(FileOutputStream o = new FileOutputStream(pwFile))
        {
            o.write("this is a fairly long password".getBytes());
        }

        Set<PosixFilePermission> permission = new HashSet<>();
        permission.add(PosixFilePermission.OWNER_READ);
        permission.add(PosixFilePermission.OWNER_WRITE);

        Files.setPosixFilePermissions(pwFile.toPath(), permission);

        PasswordProvider uic = new PasswordProvider();
        uic.setArchivePassword(pwFile);
    }

    @Test
    public void testFromFilePermissions() throws Exception
    {
        File pwFile = folder.newFile();
        try(FileOutputStream o = new FileOutputStream(pwFile))
        {
            o.write("this is a fairly long password".getBytes());
        }

        Set<PosixFilePermission> permission = new HashSet<>();
        permission.add(PosixFilePermission.OWNER_READ);
        permission.add(PosixFilePermission.OWNER_WRITE);
        permission.add(PosixFilePermission.OTHERS_READ);

        Files.setPosixFilePermissions(pwFile.toPath(), permission);

        PasswordProvider uic = new PasswordProvider();

        try
        {
            uic.setArchivePassword(pwFile);
            fail("Should have thrown bad error");
        }
        catch (IllegalPasswordException ignored) {}

        permission.remove(PosixFilePermission.OTHERS_READ);
        permission.add(PosixFilePermission.OTHERS_WRITE);

        Files.setPosixFilePermissions(pwFile.toPath(), permission);

        try
        {
            uic.setArchivePassword(pwFile);
            fail("Should have thrown bad error");
        }
        catch (IllegalPasswordException ignored) {}
    }
}
