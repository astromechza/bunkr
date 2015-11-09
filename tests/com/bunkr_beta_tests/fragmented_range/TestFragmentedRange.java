package com.bunkr_beta_tests.fragmented_range;

import com.bunkr_beta.fragmented_range.FragmentedRange;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class TestFragmentedRange
{
    @Test
    public void testEmptyConstruct()
    {
        FragmentedRange fr = new FragmentedRange();
        assertTrue(fr.isEmpty());
        assertFalse(fr.contains(1));
        assertEquals(fr.size(), 0);
        assertTrue(fr.toList().isEmpty());
        FragmentedRange fr2 = fr.copy();
        assertTrue(fr2.isEmpty());
        assertFalse(fr2.contains(1));
        assertEquals(fr2.size(), 0);
        assertTrue(fr2.toList().isEmpty());
    }

    @Test
    public void testSimpleConstruct()
    {
        FragmentedRange fr = new FragmentedRange(10, 100);
        assertFalse(fr.isEmpty());
        assertFalse(fr.contains(9));
        assertTrue(fr.contains(10));
        assertTrue(fr.contains(109));
        assertFalse(fr.contains(110));
        assertEquals(fr.size(), 100);
        assertEquals(fr.toList().size(), 100);
    }

    @Test
    public void testSimpleConstructNegative()
    {
        FragmentedRange fr = new FragmentedRange(-20, 88);
        assertFalse(fr.isEmpty());
        assertFalse(fr.contains(-21));
        assertTrue(fr.contains(-20));
        assertTrue(fr.contains(67));
        assertFalse(fr.contains(68));
        assertEquals(fr.size(), 88);
        assertEquals(fr.toList().size(), 88);
    }

    @Test
    public void testAddIndex()
    {
        FragmentedRange fr = new FragmentedRange(10, 100);
        fr.add(200);
        assertEquals(fr.size(), 101);
        assertEquals(fr.getMax(), 200);
        fr.add(10);
        assertEquals(fr.size(), 101);
        fr.add(0);
        assertEquals(fr.size(), 102);
        fr.add(-1212312310);
        assertEquals(fr.size(), 103);
    }

    @Test
    public void testAddRange()
    {
        FragmentedRange fr = new FragmentedRange(10, 20);
        fr.add(31, 10);
        assertTrue(fr.contains(29));
        assertFalse(fr.contains(30));
        assertTrue(fr.contains(31));
        assertEquals(fr.size(), 30);

        fr.add(8, 6);
        assertEquals(fr.size(), 32);
        assertEquals(fr.toList(), Arrays.asList(
                8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                29, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40
        ));
        assertEquals(fr.getMin(), 8);
        fr.add(1, 0);
        assertEquals(fr.getMin(), 8);

        try
        {
            fr.add(2, -1);
            fail("Should have raised an Exception");
        }
        catch (IllegalArgumentException ignored)
        {}
    }

    @Test
    public void testRemove()
    {
        FragmentedRange fr = new FragmentedRange(1, 12);
        assertEquals(fr.size(), 12);
        assertTrue(fr.contains(8));
        fr.remove(8);
        assertTrue(fr.contains(7));
        assertTrue(fr.contains(9));
        assertEquals(fr.size(), 11);
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12
        ));
    }

    @Test
    public void testRemoveRange()
    {
        FragmentedRange fr = new FragmentedRange(1, 20);
        fr.remove(5, 5);
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        ));
        fr.remove(7, 7);
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 14, 15, 16, 17, 18, 19, 20
        ));
        assertEquals(fr.size(), 11);
        fr.remove(2, 5);
        assertEquals(fr.toList(), Arrays.asList(
                1, 14, 15, 16, 17, 18, 19, 20
        ));
        assertEquals(fr.size(), 8);
        fr.remove(21, 2);
        assertEquals(fr.toList(), Arrays.asList(
                1, 14, 15, 16, 17, 18, 19, 20
        ));
        assertEquals(fr.size(), 8);
    }

    @Test
    public void testPops()
    {
        FragmentedRange fr = new FragmentedRange(1, 20);
        fr.remove(8, 5);
        ArrayList<Integer> maxPopped = new ArrayList<>();
        ArrayList<Integer> minPopped = new ArrayList<>();
        while(!fr.isEmpty())
        {
            maxPopped.add(fr.popMax());
            if (!fr.isEmpty()) minPopped.add(fr.popMin());
        }

        assertEquals(maxPopped, Arrays.asList(
                20, 19, 18, 17, 16, 15, 14, 13
        ));

        assertEquals(minPopped, Arrays.asList(
                1, 2, 3, 4, 5, 6, 7
        ));

        try
        {
            fr.popMax();
            fail("empty pop max should raise error");
        }
        catch (IndexOutOfBoundsException ignored) {}

        try
        {
            fr.popMin();
            fail("empty pop min should raise error");
        }
        catch (IndexOutOfBoundsException ignored) {}
    }

    @Test
    public void testClear()
    {
        FragmentedRange fr = new FragmentedRange(1, 20);
        fr.remove(8, 5);
        assertFalse(fr.isEmpty());
        fr.clear();
        assertTrue(fr.isEmpty());
        fr.clear();
        assertTrue(fr.isEmpty());
    }

    @Test
    public void testUnion()
    {
        FragmentedRange fr = new FragmentedRange(1, 4);
        fr.union(new FragmentedRange(8, 4));
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 8, 9, 10, 11
        ));
        fr.union(new FragmentedRange(5, 1));
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 8, 9, 10, 11
        ));
    }

    @Test
    public void testStaticUnion()
    {
        FragmentedRange frA = new FragmentedRange(1, 4);
        FragmentedRange frB = new FragmentedRange(4, 4);
        FragmentedRange frC = FragmentedRange.union(frA, frB);
        assertEquals(frA.toList(), Arrays.asList(
                1, 2, 3, 4
        ));
        assertEquals(frB.toList(), Arrays.asList(
                4, 5, 6, 7
        ));
        assertEquals(frC.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7
        ));
    }

    @Test
    public void testSubtract()
    {
        FragmentedRange fr = new FragmentedRange(1, 17);
        fr.subtract(new FragmentedRange(8, 4));
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17
        ));
        fr.subtract(new FragmentedRange(15, 1));
        assertEquals(fr.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 16, 17
        ));
    }

    @Test
    public void testStaticSubtract()
    {
        FragmentedRange frA = new FragmentedRange(1, 17);
        FragmentedRange frB = new FragmentedRange(8, 4);
        FragmentedRange frC = FragmentedRange.subtract(frA, frB);
        assertEquals(frA.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
        ));
        assertEquals(frB.toList(), Arrays.asList(
                8, 9, 10, 11
        ));
        assertEquals(frC.toList(), Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17
        ));
    }

    @Test
    public void testCopy()
    {
        FragmentedRange frA = new FragmentedRange(-10, 7);
        FragmentedRange frB = frA.copy();
        frA.remove(-7);
        assertEquals(frA.size(), 6);
        assertEquals(frB.size(), 7);
        assertTrue(frB.contains(-7));
        frB.clear();
        assertEquals(frA.size(), 6);
    }
}
