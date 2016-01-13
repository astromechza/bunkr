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

package org.bunkr.cli.commands;

import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.cli.CLIPasswordPrompt;
import org.bunkr.core.exceptions.BaseBunkrException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public interface ICLICommand
{
    void buildParser(Subparser target);
    void handle(Namespace args) throws Exception;

    default PasswordProvider makeCLIPasswordProvider(File pwFile, String prompt) throws IOException, BaseBunkrException
    {
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt(prompt));
        if (pwFile != null && ! pwFile.getPath().equals("-")) passProv.setArchivePassword(pwFile);
        return passProv;
    }

    default PasswordProvider makeCLIPasswordProvider(File pwFile) throws IOException, BaseBunkrException
    {
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (pwFile != null && ! pwFile.getPath().equals("-")) passProv.setArchivePassword(pwFile);
        return passProv;
    }
}
