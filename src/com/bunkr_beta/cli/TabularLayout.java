package com.bunkr_beta.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creator: benmeier
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
