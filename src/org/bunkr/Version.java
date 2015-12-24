package org.bunkr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public final class Version
{
    public static final String versionString;
    public static final byte versionMajor;
    public static final byte versionMinor;
    public static final byte versionBugfix;
    public static final String compatibleVersionString;
    private static final byte compatibleVersionMajor;
    private static final byte compatibleVersionMinor;
    private static final byte compatibleVersionBugfix;
    public static final String gitDate;
    public static final String gitHash;

    static
    {
        String tversionNumber = "0.0.0";
        String tcompatVersionNumber = "0.0.0";
        String tgitDate = "1970-01-01T00:00:00+00:00";
        String tgitHash = "????????????????????????????????????????";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Resources.getStream("/version.dat"))))
        {
            tversionNumber = br.readLine();
            tcompatVersionNumber = br.readLine();
            tgitDate = br.readLine();
            tgitHash = br.readLine();
        }
        catch (IOException | NullPointerException ignored) { }

        versionString = tversionNumber;
        compatibleVersionString = tcompatVersionNumber;
        gitDate = tgitDate;
        gitHash = tgitHash;

        String[] parts = versionString.split("\\.", -1);
        versionMajor = Byte.parseByte(parts[0]);
        versionMinor = Byte.parseByte(parts[1]);
        versionBugfix = Byte.parseByte(parts[2]);

        parts = compatibleVersionString.split("\\.", -1);
        compatibleVersionMajor = Byte.parseByte(parts[0]);
        compatibleVersionMinor = Byte.parseByte(parts[1]);
        compatibleVersionBugfix = Byte.parseByte(parts[2]);
    }

    /**
     * Need some way of declaring and checking backward compatibility.
     *
     * Keep the earliestCompatibleVersion numbers up to date as breaking changes are made to the meta data. This means
     * that opening a file that is too old will bring up an error.
     *
     * The Boolean 'strict' will cause the function to return false when the file was created with a newer version of
     * the software. This is optional since the user may want to just try to open the file anyway.
     */
    public static boolean isCompatible(byte major, byte minor, byte bugfix, boolean strict)
    {
        int currentVersion = (versionMajor * 256 + versionMinor) * 256 + versionBugfix;
        int inputVersion = (major * 256 + minor) * 256 + bugfix;
        int earliestVersion = (compatibleVersionMajor * 256 + compatibleVersionMinor) * 256 + compatibleVersionBugfix;
        return inputVersion >= earliestVersion && !(strict && inputVersion > currentVersion);
    }

    /**
     * Similar to isCompatible() but as an assertion.
     * @throws IOException if not compatible.
     */
    public static void assertCompatible(byte major, byte minor, byte bugfix, boolean strict) throws IOException
    {
        if (! isCompatible(major, minor, bugfix, strict)) throw new IOException(
                String.format(
                        "Archive version %d.%d.%d is not compatible with Application version range %s - %s",
                        major, minor, bugfix,
                        compatibleVersionString,
                        versionString
                )
        );
    }
}
