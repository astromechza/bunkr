package org.bunkr.core.usersec;

import org.bunkr.core.exceptions.IllegalPasswordException;

import javax.xml.bind.DatatypeConverter;

/**
 * Creator: benmeier
 * Created At: 2015-12-25
 */
public class PasswordRequirements
{
    public static final int MINIMUM_PASSWORD_LENGTH = 8;

    public static void checkPasses(byte[] input) throws IllegalPasswordException
    {
        if (input.length < MINIMUM_PASSWORD_LENGTH) throw new IllegalPasswordException(
                "Password must be at least %d characters.",
                MINIMUM_PASSWORD_LENGTH
        );

        for (byte b : input)
        {
            if (b < (byte) 0x20) throw new IllegalPasswordException(
                    "Password cannot contain byte %s",
                    DatatypeConverter.printByte(b)
            );
            if (b > (byte) 0x7E) throw new IllegalPasswordException(
                    "Password cannot contain byte %s",
                    DatatypeConverter.printByte(b)
            );
        }
    }
}
