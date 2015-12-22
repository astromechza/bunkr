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
    public static final String versionNumber;
    public static final byte versionMajor;
    public static final byte versionMinor;
    public static final byte versionBugfix;
    public static final String gitDate;
    public static final String gitHash;

    private static final byte earliestCompatibleVersionMajor = 0;
    private static final byte earliestCompatibleVersionMinor = 0;
    private static final byte earliestCompatibleVersionBugfix = 0;

    static
    {
        String tversionNumber = "0.0.0";
        String tgitDate = "1970-01-01T00:00:00+00:00";
        String tgitHash = "????????????????????????????????????????";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Version.class.getResourceAsStream("/version.dat"))))
        {
            tversionNumber = br.readLine();
            tgitDate = br.readLine();
            tgitHash = br.readLine();
        }
        catch (IOException | NullPointerException ignored) { }

        versionNumber = tversionNumber;
        gitDate = tgitDate;
        gitHash = tgitHash;

        String[] parts = versionNumber.split("\\.", -1);
        versionMajor = Byte.parseByte(parts[0]);
        versionMinor = Byte.parseByte(parts[1]);
        versionBugfix = Byte.parseByte(parts[2]);
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
        int earliestVersion = (earliestCompatibleVersionMajor * 256 + earliestCompatibleVersionMinor) * 256 +
                earliestCompatibleVersionBugfix;
        return inputVersion >= earliestVersion && !(strict && inputVersion > currentVersion);
    }

    public static void assertCompatible(byte major, byte minor, byte bugfix, boolean strict) throws IOException
    {
        if (! isCompatible(major, minor, bugfix, strict)) throw new IOException(
                String.format(
                        "Archive version %d.%d.%d is not compatible with Application version %d.%d.%d",
                        major, minor, bugfix,
                        versionMajor, versionMinor, versionBugfix
                )
        );
    }

}
