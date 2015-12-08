package com.bunkr_beta.inventory;

import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;

import java.util.regex.Pattern;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class InventoryPather
{
    public static final Pattern namePattern = Pattern.compile("[0-9a-zA-Z\\-_\\.,;]+");
    public static final Pattern pathPattern = Pattern.compile("^(?:/" + namePattern.pattern() + ")+|/$");

    public static boolean isValid(String path)
    {
        return pathPattern.matcher(path).matches();
    }

    public static String assertValid(String path)
    {
        if (path.length() > 1 && path.charAt(path.length() - 1) == '/') path = path.substring(0, path.length() - 1);
        if (! isValid(path))
            throw new IllegalPathException(String.format("Error: '%s' is not a valid path.", path));
        return path;
    }

    public static String[] getParts(String path)
    {
        path = assertValid(path);
        if (path.equals("/")) return new String[]{};
        return path.substring(1).split("/");
    }

    public static String dirname(String path)
    {
        path = assertValid(path);
        if (path.equals("/")) return "/";
        String s = path.substring(0, path.lastIndexOf("/"));
        if (s.equals("")) return "/";
        return s;
    }

    public static String baseName(String path)
    {
        path = assertValid(path);
        if (path.equals("/")) return "";
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String simpleJoin(String path, String n)
    {
        path = assertValid(path);
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
}
