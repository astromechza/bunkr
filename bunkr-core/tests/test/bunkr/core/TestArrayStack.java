package test.bunkr.core;

import org.bunkr.core.utils.ArrayStack;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Creator: benmeier
 * Created At: 2015-11-28
 */
public class TestArrayStack
{
    @Test
    public void testOperations()
    {
        ArrayStack<Integer> a = new ArrayStack<>();
        assertThat(a.isEmpty(), is(true));
        try
        {
            a.peek();
            fail("Should have thrown array out of bounds");
        }
        catch (IndexOutOfBoundsException ignored) {}
        try
        {
            a.pop();
            fail("Should have thrown array out of bounds");
        }
        catch (IndexOutOfBoundsException ignored) {}

        a.push(1);

        assertThat(a.isEmpty(), is(false));
        assertThat(a.peek(), is(equalTo(1)));
        assertThat(a.pop(), is(equalTo(1)));
        assertThat(a.isEmpty(), is(true));

        a.push(2);
        a.push(3);
        a.push(4);

        assertThat(a.peek(), is(equalTo(4)));
        assertThat(a.pop(), is(equalTo(4)));
        assertThat(a.peek(), is(equalTo(3)));
        assertThat(a.pop(), is(equalTo(3)));
        assertThat(a.peek(), is(equalTo(2)));
        assertThat(a.pop(), is(equalTo(2)));
        assertThat(a.isEmpty(), is(true));
    }

    @Test
    public void testIterations()
    {
        ArrayStack<Integer> a = new ArrayStack<>();
        a.push(2);
        a.push(4);
        a.push(6);
        a.push(8);

        List<Integer> upOrder = new ArrayList<>();
        a.bottomToTop().forEachRemaining(upOrder::add);
        List<Integer> upOrderTrue = new ArrayList<>();
        upOrderTrue.add(2);
        upOrderTrue.add(4);
        upOrderTrue.add(6);
        upOrderTrue.add(8);

        assertThat(upOrder, is(equalTo(upOrderTrue)));

        List<Integer> downOrder = new ArrayList<>();
        a.topToBottom().forEachRemaining(downOrder::add);
        List<Integer> downOrderTrue = new ArrayList<>();
        downOrderTrue.add(8);
        downOrderTrue.add(6);
        downOrderTrue.add(4);
        downOrderTrue.add(2);

        assertThat(downOrder, is(equalTo(downOrderTrue)));
    }
}
