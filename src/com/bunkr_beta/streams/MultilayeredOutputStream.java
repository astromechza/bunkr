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
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class MultilayeredOutputStream extends OutputStream
{
    private StreamStack<OutputStream> streams = new StreamStack<>();

    private OutputStream topStream;

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target) throws IOException
    {
        this.streams.push(
                new BlockWriterOutputStream(
                        context.filePath,
                        context.getBlockSize(),
                        target,
                        new BlockAllocationManager(context, target)
                )
        );
        if (context.getArchiveDescriptor().encryption != null)
        {
            SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
            fileCipher.init(
                    true,
                    new ParametersWithIV(new KeyParameter(target.getEncryptionKey()), target.getEncryptionIV())
            );
            this.streams.push(
                    new CustomCipherOutputStream(
                            new NonClosableOutputStream(this.streams.get(0)),
                            new BufferedBlockCipher(fileCipher)
                    )
            );
        }
        if (context.getArchiveDescriptor().compression != null)
        {
            this.streams.push(new DeflaterOutputStream(this.streams.get(0)));
        }
        this.topStream = this.streams.peek();
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
        for (OutputStream stream : streams)
        {
            stream.flush();
        }
    }

    @Override
    public void close() throws IOException
    {
        for (OutputStream stream : streams)
        {
            stream.close();
        }
    }

    public class StreamStack<T> extends ArrayList<T>
    {
        public void push(T t)
        {
            this.add(0, t);
        }

        public T peek()
        {
            return this.get(0);
        }
    }
}
