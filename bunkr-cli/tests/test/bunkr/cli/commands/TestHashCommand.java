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

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.HashCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import test.bunkr.core.XTemporaryFolder;
import test.bunkr.cli.OutputCapture;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created At: 2015-12-15
 */
public class TestHashCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new HashCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    public File buildArchive() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);

        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        context.getInventory().addFile(fileOne);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
        {
            bwos.write("The quick brown fox jumps over the lazy dog".getBytes());
        }

        FolderInventoryItem folderOne = new FolderInventoryItem("folder");

        FileInventoryItem fileTwo = new FileInventoryItem("b.txt");
        context.getInventory().addFile(fileTwo);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
        {
            bwos.write(RandomMaker.get(50 * 8));
        }

        folderOne.addFile(fileTwo);
        context.getInventory().addFolder(folderOne);

        MetadataWriter.write(context, usp);

        return archiveFile;
    }

    public void checkHash(String algorithm, String expectedHash) throws Exception
    {
        File archive = buildArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(HashCommand.ARG_PATH, "/a.txt");
        args.put(HashCommand.ARG_ALGORITHM, algorithm);

        try(OutputCapture c = new OutputCapture())
        {
            new HashCommand().handle(new Namespace(args));
            c.close();
            assertThat(c.getContent(), containsString("=============="));
            assertThat(c.getContent(), containsString("|  0%"));
            assertThat(c.getContent(), containsString("|100%"));
            assertThat(c.getContent().trim(), endsWith(expectedHash));
        }
    }

    public void checkHashNoProgress(String algorithm, String expectedHash) throws Exception
    {
        File archive = buildArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(HashCommand.ARG_PATH, "/a.txt");
        args.put(HashCommand.ARG_ALGORITHM, algorithm);
        args.put(HashCommand.ARG_NO_PROGRESS, true);

        try(OutputCapture c = new OutputCapture())
        {
            new HashCommand().handle(new Namespace(args));
            c.close();
            assertThat(c.getContent(), not(containsString("==============")));
            assertThat(c.getContent(), not(containsString("|  0%")));
            assertThat(c.getContent(), not(containsString("|100%")));
            assertThat(c.getContent().trim(), endsWith(expectedHash));
        }
    }

    @Test
    public void testMD5() throws Exception
    {
        checkHash("md5", "9e107d9d372bb6826bd81d3542a419d6");
    }

    @Test
    public void testSHA1() throws Exception
    {
        checkHash("sha1", "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12");
    }

    @Test
    public void testSHA224() throws Exception
    {
        checkHash("sha224", "730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525");
    }

    @Test
    public void testSHA256() throws Exception
    {
        checkHash("sha256", "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592");
    }

    @Test
    public void testNoProgress() throws Exception
    {
        checkHashNoProgress("md5", "9e107d9d372bb6826bd81d3542a419d6");
    }

    @Test
    public void testUnknown() throws Exception
    {
        try
        {
            checkHash("wtfmate", "??");
            fail("Should raise failure");
        } catch (CLIException ignored) {}
    }

}
