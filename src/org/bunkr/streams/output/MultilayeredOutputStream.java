package org.bunkr.streams.output;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.BlockAllocationManager;
import org.bunkr.streams.AlgorithmIdentifier;
import org.bunkr.utils.RandomMaker;
import org.bunkr.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class MultilayeredOutputStream extends OutputStream
{
    private final FileInventoryItem target;

    private OutputStream topstream;
    private long writtenBytes = 0;

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target, boolean refreshKeys)
            throws FileNotFoundException
    {
        this.target = target;
        this.topstream = new BlockWriterOutputStream(
                    context.filePath,
                    context.getBlockSize(),
                    target,
                    new BlockAllocationManager(context.getInventory(), target.getBlocks())
        );

        AlgorithmIdentifier aid = target.getAlgorithms();
        if(aid.getEngine() != null) addOnEncryptionStream(aid, target.getEncryptionData(), refreshKeys);
        if(aid.getCompressor() != null) addOnCompressorStream(aid);
    }

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target) throws FileNotFoundException
    {
        this(context, target, true);
    }

    @Override
    public void write(int b) throws IOException
    {
        this.topstream.write(b);
        writtenBytes += 1;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.topstream.write(b);
        writtenBytes += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.topstream.write(b, off, len);
        writtenBytes += len;
    }

    @Override
    public void flush() throws IOException
    {
        this.topstream.flush();
    }

    @Override
    public void close() throws IOException
    {
        this.topstream.close();
        target.setActualSize(this.writtenBytes);
    }


    private void addOnEncryptionStream(AlgorithmIdentifier aid, byte[] encryptionData, boolean refreshKeys)
    {
        BlockCipher baseCipher;
        CipherParameters cipherParameters;
        if (aid.getEngine() == AlgorithmIdentifier.Engine.AES256)
        {
            int blockLength = 256 / 8;
            byte[] edata = target.getEncryptionData();
            if (edata == null)
            {
                edata = RandomMaker.get(256 * 2);
                target.setencryptionData(edata);
            }
            else if (refreshKeys)
            {
                RandomMaker.fill(edata);
                target.setencryptionData(edata);
            }
            baseCipher = new AESEngine();

            byte[] ekey = Arrays.copyOfRange(encryptionData, 0, blockLength);
            byte[] eiv = Arrays.copyOfRange(encryptionData, blockLength, encryptionData.length);

            cipherParameters = new ParametersWithIV(new KeyParameter(ekey), eiv);
        }
        else
        {
            throw new IllegalArgumentException("Encryption algorithm not implemented on MultilayeredInputStream");
        }

        if (aid.getAlgorithm() == AlgorithmIdentifier.Algorithm.CTR)
        {
            baseCipher = new SICBlockCipher(baseCipher);
        }

        baseCipher.init(true, cipherParameters);
        this.topstream = new CipherOutputStream(this.topstream, new BufferedBlockCipher(baseCipher));
    }

    private void addOnCompressorStream(AlgorithmIdentifier aid)
    {
        if (aid.getCompressor() == AlgorithmIdentifier.Compressor.DEFLATE)
        {
            this.topstream = new DeflaterOutputStream(this.topstream, new Deflater(Deflater.BEST_SPEED));
        }
        else
        {
            throw new IllegalArgumentException("Compressor not implemented on MultilayeredInputStream");
        }
    }
}
