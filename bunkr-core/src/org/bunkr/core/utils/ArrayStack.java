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

package org.bunkr.core.utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 *
 * This is a stack based on an array list.
 * Basically just a convenience wrapper around an array list.
 */
public class ArrayStack<T>
{
    private final ArrayList<T> items = new ArrayList<>();

    public void push(T t)
    {
        this.items.add(0, t);
    }

    public T peek()
    {
        return this.items.get(0);
    }

    public boolean isEmpty()
    {
        return this.items.isEmpty();
    }

    public T pop()
    {
        T t = this.peek();
        this.items.remove(0);
        return t;
    }

    public Iterator<T> topToBottom()
    {
        return new Iterator<T>() {
            int index = 0;

            @Override
            public boolean hasNext()
            {
                return ArrayStack.this.items.size() > index;
            }

            @Override
            public T next()
            {
                return ArrayStack.this.items.get(index++);
            }
        };
    }

    public Iterator<T> bottomToTop()
    {
        return new Iterator<T>() {
            int index = ArrayStack.this.items.size() - 1;

            @Override
            public boolean hasNext()
            {
                return index > -1;
            }

            @Override
            public T next()
            {
                return ArrayStack.this.items.get(index--);
            }
        };
    }
}
