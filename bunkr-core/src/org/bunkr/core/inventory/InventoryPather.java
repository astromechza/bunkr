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

package org.bunkr.core.inventory;

import org.bunkr.core.exceptions.IllegalPathException;
import org.bunkr.core.exceptions.TraversalException;

import java.util.regex.Pattern;

/**
 * Created At: 2015-12-06
 */
public class InventoryPather
{
    public static final Pattern namePattern = Pattern.compile("[^/:*?\"<>\\x00-\\x1F\\x7F]+");
    public static final Pattern pathPattern = Pattern.compile("^(?:/" + namePattern.pattern() + ")+|/$");
    public static final Pattern relpathPattern = Pattern.compile("^(?:\\.\\./)*(?:" + namePattern.pattern() + "(?:/" + namePattern.pattern() + ")*)?");

    public static boolean isValidPath(String path)
    {
        return pathPattern.matcher(path).matches();
    }

    public static boolean isValidName(String name)
    {
        return namePattern.matcher(name).matches();
    }

    public static boolean isValidRelativePath(String path)
    {
        return relpathPattern.matcher(path).matches();
    }

    public static String assertValidPath(String path)
    {
        path = path.trim();
        if (path.length() > 1 && path.charAt(path.length() - 1) == '/') path = path.substring(0, path.length() - 1);
        if (! isValidPath(path))
            throw new IllegalPathException(String.format("Error: '%s' is not a valid path.", path));
        return path;
    }

    public static String assertValidName(String name)
    {
        name = name.trim();
        if (! isValidName(name))
            throw new IllegalPathException(String.format("Error: '%s' is not a valid name.", name));
        return name;
    }

    public static String assertValidRelativePath(String path)
    {
        path = path.trim();
        if (! isValidRelativePath(path))
            throw new IllegalPathException(String.format("Error: '%s' is not a valid relative path.", path));
        return path;
    }

    public static String[] getParts(String path)
    {
        path = assertValidPath(path);
        if (path.equals("/")) return new String[]{};
        return path.substring(1).split("/");
    }

    public static String dirname(String path)
    {
        path = path.trim();
        if (path.equals("/") || ! path.contains("/")) return "/";
        String s = path.substring(0, path.lastIndexOf("/"));
        if (s.equals("")) return "/";
        return s;
    }

    public static String baseName(String path)
    {
        path = path.trim();
        if (path.equals("/")) return "";
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String simpleJoin(String path, String n)
    {
        path = assertValidPath(path);
        if (path.equals("/")) return "/" + n;
        return path + "/" + n;
    }

    /**
     * Traverse the inventory for a path
     */
    public static IFFTraversalTarget traverse(Inventory inventory, String path) throws TraversalException
    {
        // this will also do a validity check
        String[] pathParts = getParts(path);

        // if no parts, return inventory
        if (pathParts.length == 0) return inventory;

        // start at the inventory
        String pathSoFar = "/";
        IFFContainer current = inventory;
        for (int i = 0; i < pathParts.length - 1; i++)
        {
            IFFContainer c = current.findFolder(pathParts[i]);
            if (c == null)
                throw new TraversalException(String.format("No such folder '%s' in '%s'", pathParts[i], pathSoFar));
            current = c;
            pathSoFar += pathParts[i] + "/";
        }

        IFFTraversalTarget t = current.findFileOrFolder(pathParts[pathParts.length - 1]);
        if (t == null)
            throw new TraversalException(String.format("No such file or folder '%s' in '%s'", pathParts[pathParts.length - 1], pathSoFar));
        return t;
    }

    public static String applyRelativePath(String original, String relative)
    {
        assertValidPath(original);
        assertValidRelativePath(relative);

        if (relative.endsWith("..")) relative += "/";

        while (relative.startsWith("../"))
        {
            original = InventoryPather.dirname(original);
            relative = relative.substring(3);
        }

        if (relative.length() > 0)
        {
            if (original.equals("/"))
            {
                original = "/" + relative;
            }
            else
            {
                original = original + "/" + relative;
            }
        }

        return original;
    }
}
