package com.bunkr_beta.demos;

import com.bunkr_beta.*;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.streams.CustomCipherOutputStream;
import com.bunkr_beta.streams.NonClosableOutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.UUID;

public class BuildExampleArchive2
{
    public static void run()
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException
    {

        SecureRandom r = new SecureRandom();

        int keysize =  256 / 8;
        int blocksize = 1024;
        int singlefilesize = 1500;

        byte[] encryptionKey = new byte[keysize];
        r.nextBytes(encryptionKey);
        byte[] encryptionIV = new byte[keysize];
        r.nextBytes(encryptionIV);

        // create the inventory
        int numBlocks = (int) Math.ceil(singlefilesize / (float) blocksize);
        ArrayList<FileInventoryItem> files = new ArrayList<>();
        files.add(
                new FileInventoryItem("example", UUID.randomUUID(), new FragmentedRange(0, 2), singlefilesize, System.currentTimeMillis(),
                                       encryptionKey, encryptionIV));
        Inventory inventory = new Inventory(files, new ArrayList<>());
        StringWriter isw = new StringWriter();
        new ObjectMapper().writeValue(isw, inventory);

        Descriptor ad = new Descriptor(null, null);
        StringWriter asw = new StringWriter();
        new ObjectMapper().writeValue(asw, ad);


        // now open the stream!
        try(FileOutputStream fos = new FileOutputStream("example.bnkr"))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write("BUNKR".getBytes());
                dos.write(new byte[]{ 0, 0, 0 });
                dos.writeInt(blocksize);
                dos.writeLong(numBlocks * blocksize);

                SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
                fileCipher.init(true, new ParametersWithIV(new KeyParameter(encryptionKey), encryptionIV));
                try (CustomCipherOutputStream cos = new CustomCipherOutputStream(new NonClosableOutputStream(dos), new BufferedBlockCipher(fileCipher)))
                {
                    cos.write(new byte[singlefilesize]);
                }
                byte[] filler = new byte[blocksize - (singlefilesize % blocksize)];
                r.nextBytes(filler);
                dos.write(filler);
                dos.writeInt(isw.toString().length());
                dos.writeBytes(isw.toString());
                dos.writeInt(asw.toString().length());
                dos.writeBytes(asw.toString());
            }
        }


        IArchiveInfoContext aic = new ArchiveInfoContext(new File("example.bnkr"));

    }
}
