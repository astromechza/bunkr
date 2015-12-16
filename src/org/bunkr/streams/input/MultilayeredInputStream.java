package org.bunkr.streams.input;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class MultilayeredInputStream extends InputStream
{
    private final BlockReaderInputStream baseStream;
    private final boolean emptyFile;

    private InputStream topstream;

    public MultilayeredInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this.emptyFile = target.getActualSize() == 0;

        this.baseStream = new BlockReaderInputStream(context.filePath, context.getBlockSize(), target);
        this.topstream = this.baseStream;

        if (context.getDescriptor().hasEncryption())
        {
            SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
            fileCipher.init(
                    false,
                    new ParametersWithIV(new KeyParameter(target.getEncryptionKey()), target.getEncryptionIV())
            );

            this.topstream = new CipherInputStream(this.topstream, new BufferedBlockCipher(fileCipher));
        }

        if (context.getDescriptor().hasCompression())
        {
            this.topstream = new InflaterInputStream(this.topstream);
        }
    }

    @Override
    /**
     * NOTE: this will return some value in bytes, not necessarily the total length of the stream, just a number of
     * bytes that can be read immediately without blocking.
     */
    public int available() throws IOException
    {
        return this.topstream.available();
    }

    @Override
    public long skip(long n) throws IOException
    {
        return this.topstream.skip(n);
    }

    @Override
    public int read() throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        topstream.close();
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    public void setCheckHashOnFinish(boolean b)
    {
        this.baseStream.setCheckHashOnFinish(b);
    }
}
