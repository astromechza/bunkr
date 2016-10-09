/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core.fragmented_range;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The FragmentedRange object can be used to represent a list of integers with high density. It is best suited to
 * scenarios where the integers consist of multiple ranges of continuous integers.
 *
 * > FragmentedRange f = new FragmentedRange()
 * > f.add(4, 10)
 * now f contains 4,5,6,7,8,9,10,11,12,13
 * > f.remove(6)
 * now f contains 4,5, 7,8,9,10,11,12,13
 * > f.add(20,2)
 * now f contains 4,5, 7,8,9,10,11,12,13, 20,21
 * as you can see it contains 3 runs of integers
 * > f.contains(12)
 * true
 * > f.contains(6)
 * false
 *
 * It can also act as a sort of Ordered Set. Items are never contained multiple times, and a kind of order is maintained
 * with getMin, popMin, getMax, popMax functionality.
 *
 * Efficiency is a little unknown and hard to describe, worst case is O(N) insert and remove with pathological inputs.
 * O(logN) for contains (probably). O(1) max and min methods.
 *
 */
public class FragmentedRange
{
    private static final int MIN_CAPACITY = 10;

    private int[] data = new int[MIN_CAPACITY];
    private int usedDataLength = 0;

    /**
     * Construct a new empty FragmentedRange object
     */
    public FragmentedRange() { }

    /**
     * Construct a FragmentedRange object which contains the given range to start with
     * @param start the number at which the range starts
     * @param length the length of the range
     */
    public FragmentedRange(int start, int length)
    {
        this.add(start, length);
    }

    /**
     * Merge all ranges from the other object into the current object.
     * @param other another range object
     */
    public void union(FragmentedRange other)
    {
        for (int i = 0; i < other.usedDataLength; i+=2)
        {
            this.add(other.data[i], other.data[i + 1]);
        }
    }

    public static FragmentedRange union(FragmentedRange fra, FragmentedRange frb)
    {
        FragmentedRange w = fra.copy();
        w.union(frb);
        return w;
    }

    public void subtract(FragmentedRange other)
    {
        for (int i = 0; i < other.usedDataLength; i+=2)
        {
            this.remove(other.data[i], other.data[i + 1]);
        }
    }

    public static FragmentedRange subtract(FragmentedRange fra, FragmentedRange frb)
    {
        FragmentedRange w = fra.copy();
        w.subtract(frb);
        return w;
    }

    /**
     * @return a deep copy of the current FragmentedRange object
     */
    public FragmentedRange copy()
    {
        FragmentedRange o = new FragmentedRange();
        o.data = Arrays.copyOf(data, data.length);
        o.usedDataLength = usedDataLength;
        return o;
    }

    /**
     * @return true if the object represents no ranges
     */
    public boolean isEmpty()
    {
        return usedDataLength < 2;
    }

    /**
     * Clear all ranges from this object
     */
    public void clear()
    {
        this.data = new int[MIN_CAPACITY];
        this.usedDataLength = 0;
    }

    /**
     * @return the number of elements in the ranges represented by this object
     */
    public int size()
    {
        int total = 0;
        for (int i = 0; i < usedDataLength; i+=2) total += data[i + 1];
        return total;
    }

    /**
     * Add a single element, this is equivalent to adding a range starting at 'index' with length 1.
     * @param index the number to add
     */
    public void add(int index)
    {
        this.add(index, 1);
    }

    /**
     * Add a range of elements.
     * @param start the number at which the range starts
     * @param length the length of the range
     */
    public void add(int start, int length)
    {
        if (length < 0) throw new IllegalArgumentException("Range length cannot be negative");
        if (length == 0) return;
        if (!isEmpty())
        {
            for (int i = 0; i < usedDataLength; i += 2)
            {
                int iStart = data[i], iLength = data[i + 1];
                if (start + length < iStart)
                {
                    addElementPair(i, start, length);
                    return;
                }
                else if (this.canCombine(start, length, iStart, iLength))
                {
                    int nStart = Math.min(start, iStart);
                    int nLength = getCombinedLength(start, length, iStart, iLength);
                    start = nStart;
                    length = nLength;
                    removeElementPair(i);
                    i -= 2;
                }
            }
        }
        addElementPair(start, length);
    }

    /**
     * Remove the value from the FragmentedRange.
     * @param start the number to remove
     */
    public void remove(int start)
    {
        this.remove(start, 1);
    }

    /**
     * Remove any items that fall within the range specified.
     *
     * eg: remove(4, 3) will remove the subrange 4,5,6
     *
     * @param qStart the start of the range
     * @param qLength the length of the range
     */
    public void remove(int qStart, int qLength)
    {
        for (int i = 0; i < usedDataLength; i+=2)
        {
            int iStart = data[i], iLength = data[i + 1];
            if (this.canCombine(qStart, qLength, iStart, iLength))
            {
                // first remove the range that intersected things
                removeElementPair(i);
                i -= 2;

                // if the query doesn't completely include range, then we
                // need to add new bits
                if (!this.completelyIncludes(qStart, qLength, iStart, iLength))
                {
                    // do we need a left section
                    if (iStart < qStart)
                    {
                        int ns = Math.min(qStart, iStart);
                        int nl = Math.abs(qStart - iStart);
                        i += 2;
                        addElementPair(i, ns, nl);
                    }

                    // do we need a right section
                    int qEnd = qStart + qLength;
                    int iEnd = iStart + iLength;
                    if (qEnd < iEnd)
                    {
                        int ns = Math.min(qEnd, iEnd);
                        int nl = Math.abs(qEnd - iEnd);
                        i += 2;
                        addElementPair(i, ns, nl);
                    }
                }
            }
        }
    }

    /**
     * @return the lowest value in the range.
     */
    public int getMin()
    {
        if (isEmpty()) throw new IndexOutOfBoundsException("Range is empty");
        return data[0];
    }

    /**
     * @return the highest value in the range.
     */
    public int getMax()
    {
        if (isEmpty()) throw new IndexOutOfBoundsException("Range is empty");
        return data[usedDataLength - 2] + data[usedDataLength - 1] - 1;
    }

    /**
     * Remove and return the lowest value in the range.
     * @return the value
     */
    public int popMin()
    {
        int v = this.getMin();
        this.remove(v);
        return v;
    }

    /**
     * Remove and return the highest value in the range.
     * @return the value
     */
    public int popMax()
    {
        int v = this.getMax();
        this.remove(v);
        return v;
    }

    /**
     * @param value the value to search for
     * @return true if the range contains the value specified.
     */
    public boolean contains(int value)
    {
        return (!this.isEmpty() && search(0, (usedDataLength / 2) - 1, value));
    }

    /**
     * @return a new ArrayList object containing all of the items in this range.
     * Warning: this could be very big if its a large span. Use .iterate() if possible.
     */
    public List<Integer> toList()
    {
        ArrayList<Integer> l = new ArrayList<>();
        for (int i = 0; i < usedDataLength; i+=2)
        {
            int a = data[i], b = data[i + 1];
            for (int j = 0; j < b; j++)
            {
                l.add(a+j);
            }
        }
        return l;
    }

    /**
     * To string method. Contains each item - could be HUGE.
     * @return a string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("FragmentedRange{");
        this.iterate().forEachRemaining(i -> sb.append(i.toString()).append(","));
        sb.append('}');
        return sb.toString();
    }

    public int[] toRepr()
    {
        int[] o = new int[usedDataLength];
        System.arraycopy(this.data, 0, o, 0, usedDataLength);
        return o;
    }

    public boolean equals(FragmentedRange other)
    {
        return Arrays.equals(this.data, other.data);
    }

    /**
     * @return an iterator over the items of the fragmented range.
     */
    public Iterator<Integer> iterate()
    {
        return new Iterator<Integer>() {
            int cursor = 0;
            int cpos = 0;
            int counter = 0;

            @Override
            public boolean hasNext()
            {
                return cursor < usedDataLength || counter > 0;
            }

            @Override
            public Integer next()
            {
                if (counter <= 0)
                {
                    cpos = data[cursor];
                    counter = data[cursor + 1];
                    cursor += 2;
                }
                counter--;
                return cpos++;
            }
        };
    }

    /**
     * @return an iterator over the ranges of the fragmented range. Each pair contains a position and length.
     */
    public Iterator<Pair<Integer, Integer>> iteratePairs()
    {
        return new Iterator<Pair<Integer, Integer>>() {
            int cursor = 0;

            @Override
            public boolean hasNext()
            {
                return cursor < usedDataLength;
            }

            @Override
            public Pair<Integer, Integer> next()
            {
                cursor += 2;
                return new Pair<>(data[cursor-2], data[cursor-1]);
            }
        };
    }

    // ======= PRIVATE PARTS ========

    //    private void rangeCheck(int index)
    //    {
    //        if (index >= usedDataLength) throw new IndexOutOfBoundsException("Index " + index + " out of range.");
    //    }
    //
    //    private void rangeCheckForAdd(int index)
    //    {
    //        if (index > usedDataLength || index < 0) throw new IndexOutOfBoundsException("Index " + index + " out of range for insert.");
    //    }

    private void ensureCapacity(int requiredCapacity)
    {
        if (requiredCapacity > data.length)
        {
            int newCapacity = Math.max(requiredCapacity, (data.length * 3) / 2 + 2);
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    private void removeElementPair(int index)
    {
        // DisableCheck: rangeCheck(index);
        int numMoved = usedDataLength - index - 2;
        if (numMoved > 0) System.arraycopy(data, index + 2, data, index, numMoved);
        usedDataLength -= 2;
    }

    private void addElementPair(int a, int b)
    {
        ensureCapacity(usedDataLength + 2);
        data[usedDataLength++] = a;
        data[usedDataLength++] = b;
    }

    private void addElementPair(int index, int a, int b)
    {
        // DisableCheck: rangeCheckForAdd(index);
        ensureCapacity(usedDataLength + 2);
        System.arraycopy(data, index, data, index + 2, usedDataLength - index);
        data[index] = a;
        data[index + 1] = b;
        usedDataLength +=2;
    }

    private boolean canCombine(int a, int al, int b, int bl)
    {
        if (a < b) return a + al >= b;
        return b + bl >= a;
    }

    private boolean completelyIncludes(int a, int al, int b, int bl)
    {
        return a <= b && (b - a + bl) <= al;
    }

    private int getCombinedLength(int a, int al, int b, int bl)
    {
        if (a == b) return Math.max(al, bl);
        int ae = a + al, be = b + bl;

        if (a < b)
        {
            if (ae >= be) return al;
            return b-a + bl;
        }
        else
        {
            if (ae <= be) return bl;
            return a - b + al;
        }
    }

    private boolean search(int si, int ei, int value)
    {
        int mi = si + ((ei - si) / 2);
        int ms = data[mi * 2];
        int me = ms + data[mi * 2 + 1];

        // if the value is on the left side of the current subrange
        if (value < ms)
        {
            // if there is a left side drop into it
            return mi != si && search(si, mi, value);
        }
        // if the value is inside the current subrange
        if (value < me) return true;

        // if there is no right side return false
        if (mi >= ei) return false;

        // otherwise drop into right range
        return search((mi + 1), ei, value);
    }
}
