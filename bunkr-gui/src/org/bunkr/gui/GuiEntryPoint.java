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

package org.bunkr.gui;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bunkr.core.Resources;
import org.bunkr.core.Version;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.windows.LandingWindow;

import java.io.File;
import java.net.URISyntaxException;

/**
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
            Font.loadFont(Resources.getExternalPath("/resources/fonts/fontawesome.ttf"), 12);
            URLRequestBlocker.instance().install();
            new LandingWindow(primaryStage).getStage().show();
        }
    }
}
