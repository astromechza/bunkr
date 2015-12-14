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
    public static final String gitDate;
    public static final String gitHash;
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
    }
}
