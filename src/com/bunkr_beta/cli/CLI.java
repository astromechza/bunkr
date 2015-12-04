package com.bunkr_beta.cli;

import com.bunkr_beta.cli.commands.AuthCommand;
import com.bunkr_beta.cli.commands.NewArchiveCommand;
import com.bunkr_beta.interfaces.ICLICommand;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CLI
{
    public static Map<String, ICLICommand> commands = new HashMap<>();
    static
    {
        commands.put("auth", new AuthCommand());
        commands.put("create", new NewArchiveCommand());
    }

    public static void main(String[] args) throws IOException
    {
        // Constructing parser and subcommands
        ArgumentParser parser = ArgumentParsers.newArgumentParser("bunkr");

        parser.addArgument("archive")
                .help("path to the archive file");

        parser.addArgument("-p", "--password-file")
                .type(String.class)
                .action(Arguments.store())
                .help("read the archive password from the given file");

        Subparsers subparsers = parser.addSubparsers().dest("subcommand");
        commands.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(e -> e.getValue().buildParser(subparsers.addParser(e.getKey())));

        try
        {
            // parse arguments from args array
            Namespace namespace = parser.parseArgs(args);

            // perform sub command and pass in args
            commands.get(namespace.getString("subcommand")).handle(namespace);
        }

        // if an exception occurs from parsing the command line inputs
        // then handle it and print help/error
        catch (ArgumentParserException e)
        {
            parser.handleError(e);
            System.exit(3);
        }
        catch (CLIException e)
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
