package com.bunkr_beta.streams.input;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.ArrayStack;
import com.bunkr_beta.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.InflaterInputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class MultilayeredInputStream extends InputStream
{
    private ArrayStack<InputStream> streams = new ArrayStack<>();

    private final boolean emptyFile;

    public MultilayeredInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this.emptyFile = target.getActualSize() == 0;

        this.streams.push(
            new BlockReaderInputStream(
                    context.filePath,
                    context.getBlockSize(),
                    target.getBlocks(),
                    target.getSizeOnDisk()
            )
        );
        if (context.getDescriptor().encryption != null)
        {
            SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
            fileCipher.init(
                    false,
                    new ParametersWithIV(new KeyParameter(target.getEncryptionKey()), target.getEncryptionIV())
            );

            this.streams.push(
                new CustomCipherInputStream(
                    new NonClosableInputStream(this.streams.peek()),
                    new BufferedBlockCipher(fileCipher)
                )
            );
        }
        if (context.getDescriptor().compression != null)
        {
            this.streams.push(new InflaterInputStream(this.streams.peek()));
        }
    }

    @Override
    public int available() throws IOException
    {
        return this.streams.peek().available();
    }

    @Override
    public long skip(long n) throws IOException
    {
        return this.streams.peek().skip(n);
    }

    @Override
    public int read() throws IOException
    {
        if(this.emptyFile) return -1;
        return this.streams.peek().read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.streams.peek().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.streams.peek().read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        Iterator<InputStream> i = this.streams.topToBottom();
        while(i.hasNext())
        {
            i.next().close();
        }
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }
}
