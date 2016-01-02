package org.bunkr.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bunkr.core.Version;
import org.bunkr.gui.windows.LandingWindow;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public class GuiEntryPoint
{
    public static void main(String[] args) throws ArgumentParserException
    {
        // Constructing parser and subcommands
        ArgumentParser parser = ArgumentParsers.newArgumentParser("bunkr");

        String entrypoint = GuiEntryPoint.class.getName();
        try
        {
            entrypoint = new File(GuiEntryPoint.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
        }
        catch (URISyntaxException ignored) { }

        parser.version(
                String.format("%s\nversion: %s\ncommit date: %s\ncommit hash: %s",
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

        Namespace namespace = parser.parseArgs(args);
        if (namespace.getBoolean("logging"))
        {
            Logging.setEnabled(true);
            Logging.info("Logging is now enabled");
        }

        MainApplication.launch(MainApplication.class);
    }

    public static class MainApplication extends Application
    {
        @Override
        public void start(Stage primaryStage) throws Exception
        {
            new URLRequestBlocker().install();

            new LandingWindow(primaryStage).getStage().show();
        }
    }
}
