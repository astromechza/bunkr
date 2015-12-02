package com.bunkr_beta_tests;

import com.bunkr_beta.UserInfoContext;
import com.bunkr_beta.interfaces.IPasswordPrompter;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class TestUserInfoContext
{
    @Test
    public void testNoPasswordOrPrompt()
    {
        UserInfoContext uic = new UserInfoContext();
        assertThat(uic.getPrompter(), is(equalTo(null)));
        uic.setArchivePassword(null);
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
        UserInfoContext uic = new UserInfoContext("Hunter2".getBytes());
        assertThat(uic.getArchivePassword(), is(equalTo("Hunter2".getBytes())));
        assertThat(uic.getPrompter(), is(equalTo(null)));

        uic.setArchivePassword(null);
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
        UserInfoContext uic = new UserInfoContext(new IPasswordPrompter() {
            @Override
            public byte[] getPassword()
            {
                return "AdventureTime".getBytes();
            }
        });

        uic.setArchivePassword(null);
        assertThat(uic.getArchivePassword(), is(equalTo("AdventureTime".getBytes())));

        uic.setArchivePassword(null);
        uic.setPrompter(null);
        try
        {
            uic.getArchivePassword();
            fail("no prompt error raised");
        }
        catch (IllegalArgumentException ignored) {}
    }

}
