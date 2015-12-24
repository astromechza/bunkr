package org.bunkr.core.streams.input;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class MultilayeredInputStream extends InputStream
{
    private final boolean emptyFile;

    private BlockReaderInputStream baseStream;
    private InputStream topstream = null;

    public MultilayeredInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this.emptyFile = target.getActualSize() == 0;
        if (! emptyFile)
        {
            this.baseStream = new BlockReaderInputStream(context.filePath, context.getBlockSize(), target);
            this.topstream = this.baseStream;

            if (context.getInventory().areFilesEncrypted())
            {
                byte[] edata = target.getEncryptionData();
                byte[] ekey = Arrays.copyOfRange(edata, 0, edata.length / 2);
                byte[] eiv = Arrays.copyOfRange(edata, edata.length / 2, edata.length);

                SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
                fileCipher.init(false, new ParametersWithIV(new KeyParameter(ekey), eiv));
                this.topstream = new CipherInputStream(this.topstream, new BufferedBlockCipher(fileCipher));
            }

            if (context.getInventory().areFilesCompressed())
            {
                this.topstream = new InflaterInputStream(this.topstream);
            }
        }
    }

    @Override
    /**
     * NOTE: this will return some value in bytes, not necessarily the total length of the stream, just a number of
     * bytes that can be read immediately without blocking.
     */
    public int available() throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.available();
    }

    @Override
    public long skip(long n) throws IOException
    {
        if(this.emptyFile) return -1;
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
        if (topstream != null) topstream.close();
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    public void setCheckHashOnFinish(boolean b)
    {
        if (this.baseStream != null) this.baseStream.setCheckHashOnFinish(b);
    }
}
