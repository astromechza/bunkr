package org.bunkr_tests;

import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.cli.passwords.IPasswordPrompter;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class TestPasswordProvider
{
    @Test
    public void testNoPasswordOrPrompt()
    {
        PasswordProvider uic = new PasswordProvider();
        assertThat(uic.getPrompter(), is(equalTo(null)));
        uic.clearArchivePassword();
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testNoPrompt()
    {
        PasswordProvider uic = new PasswordProvider();
        uic.setArchivePassword("Hunter2".getBytes());
        assertThat(uic.getHashedArchivePassword(), is(not(equalTo("Hunter2".getBytes()))));
        assertThat(uic.getPrompter(), is(equalTo(null)));

        uic.clearArchivePassword();
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testWithPrompt()
    {
        PasswordProvider uic = new PasswordProvider(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return "AdventureTime".getBytes();
            }
        });

        uic.clearArchivePassword();
        assertThat(uic.getHashedArchivePassword(), is(not(equalTo("AdventureTime".getBytes()))));

        uic.clearArchivePassword();
        uic.setPrompter(null);
        try
        {
            uic.getHashedArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

}
