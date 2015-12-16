package org.bunkr.utils;

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
