package org.bunkr.streams.input;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bunkr.streams.AlgorithmIdentifier;

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

            AlgorithmIdentifier aid = target.getAlgorithms();
            if (aid.getEngine() != null) addOnEncryptionStream(aid, target.getEncryptionData());
            if (aid.getCompressor() != null) addOnCompressorStream(aid);
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

    private void addOnEncryptionStream(AlgorithmIdentifier aid, byte[] encryptionData)
    {
        BlockCipher baseCipher;
        CipherParameters cipherParameters;
        if (aid.getEngine() == AlgorithmIdentifier.Engine.AES256)
        {
            int blockLength = 256 / 8;
            if (encryptionData.length != 2 * blockLength)
                throw new IllegalArgumentException(String.format("Not enough encryption data provided for AES256. %d != %d", encryptionData.length, 2 * blockLength));
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

        baseCipher.init(false, cipherParameters);
        this.topstream = new CipherInputStream(this.topstream, new BufferedBlockCipher(baseCipher));
    }

    private void addOnCompressorStream(AlgorithmIdentifier aid)
    {
        if (aid.getCompressor() == AlgorithmIdentifier.Compressor.DEFLATE)
        {
            this.topstream = new InflaterInputStream(this.topstream);
        }
        else
        {
            throw new IllegalArgumentException("Compressor not implemented on MultilayeredInputStream");
        }
    }
}
