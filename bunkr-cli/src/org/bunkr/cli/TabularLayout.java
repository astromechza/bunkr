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

package org.bunkr.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created At: 2015-12-09
 */
public class TabularLayout
{
    private List<String> headings = null;
    private final List<List<String>> rows = new ArrayList<>();

    public void setHeaders(String... columns)
    {
        headings = Arrays.asList(columns);
    }

    public void addRow(String... cells)
    {
        rows.add(Arrays.asList(cells));
    }

    public void printOut()
    {
        int numColumns = 0;
        if (headings != null) numColumns = Math.max(numColumns, headings.size());
        for (List<String> row : rows) numColumns = Math.max(numColumns, row.size());

        int[] columnWidths = new int[numColumns];
        if (headings != null)
        {
            for (int i = 0; i < numColumns; i++)
            {
                if (i == headings.size()) break;
                columnWidths[i] = Math.max(columnWidths[i], headings.get(i).length());
            }
        }

        for (List<String> row : rows)
        {
            for (int i = 0; i < numColumns; i++)
            {
                if (i == row.size()) break;
                columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
            }
        }

        if (headings != null)
        {
            for (int i = 0; i < headings.size(); i++)
            {
                String formatString = "%-" + columnWidths[i] + "s  ";
                System.out.print(String.format(formatString, headings.get(i)));
            }
            System.out.println();
        }
        for (List<String> row : rows)
        {
            for (int i = 0; i < row.size(); i++)
            {
                String formatString = "%-" + columnWidths[i] + "s  ";
                System.out.print(String.format(formatString, row.get(i)));
            }
            System.out.println();
        }
    }
}
