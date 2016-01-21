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

package test.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.CreateCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import test.bunkr.core.XTemporaryFolder;
import test.bunkr.cli.PasswordFile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Created At: 2015-12-11
 */
public class TestCreateCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new CreateCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void createNewArchive() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        new CreateCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        new ArchiveInfoContext(archiveFile, usp);
    }

    @Test
    public void createNewArchiveAlreadyExists() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFile();
        assertTrue(archiveFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        try
        {
            new CreateCommand().handle(new Namespace(args));
            fail("should not overwrite the file");
        }
        catch (CLIException ignored) {}
    }


    @Test
    public void createNewArchiveAlreadyExistsOverride() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFile();
        assertTrue(archiveFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, true);
        new CreateCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        new ArchiveInfoContext(archiveFile, usp);
    }
}
