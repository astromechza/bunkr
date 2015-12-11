package com.bunkr_beta_tests;

import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.cli.passwords.IPasswordPrompter;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
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
            uic.getArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testNoPrompt()
    {
        PasswordProvider uic = new PasswordProvider("Hunter2".getBytes());
        assertThat(uic.getArchivePassword(), is(equalTo("Hunter2".getBytes())));
        assertThat(uic.getPrompter(), is(equalTo(null)));

        uic.clearArchivePassword();
        try
        {
            uic.getArchivePassword();
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
        assertThat(uic.getArchivePassword(), is(equalTo("AdventureTime".getBytes())));

        uic.clearArchivePassword();
        uic.setPrompter(null);
        try
        {
            uic.getArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

}
