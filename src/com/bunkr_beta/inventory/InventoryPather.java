package com.bunkr_beta.inventory;

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

    public static void assertValid(String path)
    {
        if (! isValid(path))
            throw new IllegalArgumentException(String.format("Error: '%s' is not a valid path.", path));
    }

    public static String[] getParts(String path)
    {
        assertValid(path);
        if (path.equals("/")) return new String[]{};
        return path.substring(1).split("/");
    }

    public static String dirname(String path)
    {
        assertValid(path);
        if (path.equals("/")) return "/";
        String s = path.substring(0, path.lastIndexOf("/"));
        if (s.equals("")) return "/";
        return s;
    }

    public static String baseName(String path)
    {
        assertValid(path);
        if (path.equals("/")) return "";
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static IFFTraversalTarget findFileOrFolder(IFFContainer container, String name) throws Exception
    {
        for (FolderInventoryItem item : container.getFolders())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        for (FileInventoryItem item : container.getFiles())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    private static IFFContainer findFolder(IFFContainer container, String name) throws Exception
    {
        for (FolderInventoryItem item : container.getFolders())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    /**
     * Traverse the inventory for a path
     */
    public static IFFTraversalTarget traverse(Inventory inventory, String path) throws Exception
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
            IFFContainer c = findFolder(current, pathParts[i]);
            if (c == null)
                throw new Exception(String.format("No such folder '%s' in '%s'", pathParts[i], pathSoFar));
            current = c;
            pathSoFar += pathParts[i] + "/";
        }

        IFFTraversalTarget t = findFileOrFolder(current, pathParts[pathParts.length - 1]);
        if (t == null)
            throw new Exception(String.format("No such file or folder '%s' in '%s'", pathParts[pathParts.length - 1], pathSoFar));
        return t;
    }
}
