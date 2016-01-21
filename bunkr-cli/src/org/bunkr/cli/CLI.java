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

package org.bunkr.cli;

import org.bunkr.core.Version;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPathException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.bouncycastle.crypto.CryptoException;
import org.bunkr.cli.commands.*;
import org.bunkr.core.utils.Logging;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CLI
{
    public static final String ARG_ARCHIVE_PATH = "archive";
    public static final String ARG_PASSWORD_FILE = "passwordfile";
    public static final String ARG_SUB_COMMAND = "subcommand";

    public static final Map<String, ICLICommand> commands = new HashMap<>();
    static
    {
        commands.put("show-security", new ShowSecurityCommand());
        commands.put("check-password", new CheckPasswordCommand());
        commands.put("change-security", new ChangeSecurityCommand());
        commands.put("create", new CreateCommand());
        commands.put("mkdir", new MkdirCommand());
        commands.put("rm", new RmCommand());
        commands.put("ls", new LsCommand());
        commands.put("read", new ExportFileCommand());
        commands.put("write", new ImportFileCommand());
        commands.put("mv", new MvCommand());
        commands.put("find", new FindCommand());
        commands.put("hash", new HashCommand());
    }

    public static void main(String[] args) throws IOException
    {
        // Constructing parser and subcommands
        ArgumentParser parser = ArgumentParsers.newArgumentParser("bunkr");

        String entrypoint = CLI.class.getName();
        try
        {
            entrypoint = new File(CLI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
        }
        catch (URISyntaxException ignored) { }

        parser.version(String.format("%s\nversion: %s\ncommit date: %s\ncommit hash: %s",
                                     entrypoint,
                                     Version.versionString,
                                     Version.gitDate,
                                     Version.gitHash));
        parser.addArgument("--version").action(Arguments.version());

        parser.addArgument("--logging")
                .action(Arguments.storeTrue())
                .type(Boolean.class)
                .setDefault(false)
                .help("Enable debug logging. This may be a security issue due to information leakage.");

        parser.addArgument("archive")
                .dest(ARG_ARCHIVE_PATH)
                .type(Arguments.fileType())
                .help("path to the archive file");

        parser.addArgument("-p", "--password-file")
                .dest(ARG_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead().acceptSystemIn())
                .setDefault(new File("-"))
                .help("read the archive password from the given file");

        Subparsers subparsers = parser.addSubparsers().dest(ARG_SUB_COMMAND);
        commands.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(e -> e.getValue().buildParser(subparsers.addParser(e.getKey())));

        try
        {
            // parse arguments from args array
            Namespace namespace = parser.parseArgs(args);
            if (namespace.getBoolean("logging"))
            {
                Logging.setEnabled(true);
                Logging.info("Logging is now enabled");
            }

            Logging.info("Args: %s", namespace);

            // perform sub command and pass in args
            commands.get(namespace.getString(ARG_SUB_COMMAND)).handle(namespace);
        }

        // if an exception occurs from parsing the command line inputs
        // then handle it and print help/error
        catch (ArgumentParserException e)
        {
            parser.handleError(e);
            System.exit(3);
        }
        catch (CryptoException e)
        {
            System.err.println(String.format("Decryption failed: %s", e.getMessage()));
            System.exit(1);
        }
        catch (IllegalPathException | BaseBunkrException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Exception e)
        {
            System.err.println(String.format("Unexpected Exception %s: %s", e.getClass(), e.getMessage()));
            e.printStackTrace();
            System.exit(2);
        }
    }
}
