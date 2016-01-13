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

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.usersec.UserSecurityProvider;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CheckPasswordCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.help("test a password or password file against the archive");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        System.out.println("Decryption Succeeded");
    }
}
