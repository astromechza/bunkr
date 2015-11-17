package com.bunkr_beta.streams;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.BlockAllocationManager;
import com.bunkr_beta.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class MultilayeredOutputStream extends OutputStream
{
    private BlockWriterOutputStream coreStream;
    private CustomCipherOutputStream midStream;
    private OutputStream topStream;

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target) throws IOException
    {
        this.coreStream = new BlockWriterOutputStream(context.filePath, context.getBlockSize(), target, new BlockAllocationManager(context, target));
        SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
        fileCipher.init(true, new ParametersWithIV(new KeyParameter(target.getEncryptionKey()), target.getEncryptionIV()));
        this.midStream = new CustomCipherOutputStream(new NonClosableOutputStream(this.coreStream), new BufferedBlockCipher(fileCipher));
        this.topStream = new DeflaterOutputStream(this.midStream);
    }

    @Override
    public void write(int b) throws IOException
    {
        this.topStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.topStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.topStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        this.topStream.flush();
        this.midStream.flush();
        this.coreStream.flush();
    }

    @Override
    public void close() throws IOException
    {
        this.topStream.close();
        this.midStream.close();
        this.coreStream.close();
    }
}
