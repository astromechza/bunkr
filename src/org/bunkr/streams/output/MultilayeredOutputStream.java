package org.bunkr.streams.output;

import org.bunkr.ArchiveInfoContext;
import org.bunkr.BlockAllocationManager;
import org.bunkr.ArrayStack;
import org.bunkr.RandomMaker;
import org.bunkr.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class MultilayeredOutputStream extends OutputStream
{
    private final FileInventoryItem target;

    private final ArrayStack<OutputStream> streams = new ArrayStack<>();

    private long writtenBytes = 0;

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target, boolean refreshKeys)
    {
        this.target = target;
        this.streams.push(
            new BlockWriterOutputStream(
                    context.filePath,
                    context.getBlockSize(),
                    target,
                    new BlockAllocationManager(context.getInventory(), target.getBlocks())
            )
        );
        if (context.getDescriptor().hasEncryption())
        {
            if (refreshKeys)
            {
                target.setEncryptionKey(RandomMaker.get(context.getDescriptor().getEncryption().aesKeyLength));
                target.setEncryptionIV(RandomMaker.get(context.getDescriptor().getEncryption().aesKeyLength));
            }

            SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
            fileCipher.init(
                    true,
                    new ParametersWithIV(new KeyParameter(target.getEncryptionKey()), target.getEncryptionIV())
            );
            this.streams.push(
                new CipherOutputStream(
                        new NonClosableOutputStream(this.streams.peek()),
                        new BufferedBlockCipher(fileCipher)
                )
            );
        }
        if (context.getDescriptor().hasCompression())
        {
            this.streams.push(new DeflaterOutputStream(new NonClosableOutputStream(this.streams.peek())));
        }
    }

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this(context, target, true);
    }

    @Override
    public void write(int b) throws IOException
    {
        this.streams.peek().write(b);
        writtenBytes += 1;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.streams.peek().write(b);
        writtenBytes += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.streams.peek().write(b, off, len);
        writtenBytes += len;
    }

    @Override
    public void flush() throws IOException
    {
        Iterator<OutputStream> i = this.streams.topToBottom();
        while(i.hasNext()) i.next().flush();
    }

    @Override
    public void close() throws IOException
    {
        Iterator<OutputStream> i = this.streams.topToBottom();
        while(i.hasNext()) i.next().close();
        target.setActualSize(this.writtenBytes);
    }
}
