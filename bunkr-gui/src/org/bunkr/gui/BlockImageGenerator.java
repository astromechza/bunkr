package org.bunkr.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.utils.Logging;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created At: 2016-10-30
 */
public class BlockImageGenerator
{

    public static Image buildImageFromArchiveInfo(ArchiveInfoContext aic, int width)
    {
        FragmentedRange frange = new FragmentedRange();
        Iterator<FileInventoryItem> fit = aic.getInventory().getIterator();
        while(fit.hasNext())
        {
            frange.union(fit.next().getBlocks());
        }
        return buildImageFromFragRange(frange, width);
    }
    
    public static Image buildImageFromFragRange(FragmentedRange frange, int width)
    {
        if (frange.isEmpty()) return null;

        // block length is in bytes so we have to downshift
        int numberOfBlocks = frange.getMax() + 1;
        int numberOfPixels = Math.max(width, numberOfBlocks);
        float blocksPerPixel = numberOfBlocks / (float) numberOfPixels;
        float pixelsPerBlock = numberOfPixels / (float) numberOfBlocks;

        // new image
        BufferedImage img = new BufferedImage(numberOfPixels, 1, BufferedImage.TYPE_INT_RGB);
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        Arrays.fill(pixels, 0xFFFFFF);

        int lastPixelPainted = -1;
        Iterator<Integer> bit = frange.iterate();
        while(bit.hasNext())
        {
            int bid = bit.next();
            float pixelBucket = bid / blocksPerPixel;
            int lowBucket = Math.min(numberOfPixels - 1, (int)Math.floor(pixelBucket));

            if (lowBucket > lastPixelPainted)
            {
                pixels[lowBucket] = 0xFF0000;
                for (int i = 0, j = lowBucket; i < pixelsPerBlock; i++, j++)
                {
                    if (j < numberOfPixels)
                    {
                        pixels[j] = 0xFF0000;
                        lastPixelPainted = j - 1;
                    }
                }
            }
        }

        return SwingFXUtils.toFXImage(img, null);
    }
}
