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

import org.bunkr.core.usersec.IPasswordPrompter;

import java.io.Console;

/**
 * Created At: 2015-12-02
 */
public class CLIPasswordPrompt implements IPasswordPrompter
{
    private final String prompt;

    public CLIPasswordPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public CLIPasswordPrompt()
    {
        this("Enter password:");
    }

    @Override
    public byte[] getPassword()
    {
        Console console = System.console();
        if (console == null)
        {
            System.err.println(
                    "Couldn't get Console instance. You might not be in a pseudo terminal or may be " +
                    "redirecting stdout or stdin in some way.\n\n" +

                    "Please use the '--password-file' cli option instead in this case."
            );
            System.exit(1);
        }
        return new String(console.readPassword(this.prompt)).getBytes();
    }
}
