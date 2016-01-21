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

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Created At: 2015-11-21
 */
public class XTemporaryFolder extends TemporaryFolder
{
    public File newPrefixedFile(String prefix) throws IOException
    {
        return File.createTempFile("junit-" + prefix , "", this.getRoot());
    }

    private static final SecureRandom random = new SecureRandom();
    public File newFilePath(String prefix, String suffix)
    {
        String name = "" + prefix + Long.toString(random.nextLong()) + suffix + ".tmp";
        return new File(this.getRoot(), name);
    }
    public File newFilePath()
    {
        return this.newFilePath("", "");
    }
}
