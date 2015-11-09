package com.bunkr_beta.demos;

import com.bunkr_beta.Descriptor;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.streams.NonClosableOutputStream;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.UUID;

public class BuildExampleArchive
{
    public static void run()
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException
    {
        SecureRandom r = new SecureRandom();

        // create the inventory
        KeyGenerator aeskegen = KeyGenerator.getInstance("AES", "BC");
        byte[] ivbytes = new byte[16];
        r.nextBytes(ivbytes);
        IvParameterSpec iv = new IvParameterSpec(ivbytes);
        aeskegen.init(128);
        SecretKey sk = aeskegen.generateKey();
        ArrayList<FileInventoryItem> files = new ArrayList<>();
        files.add(
                new FileInventoryItem("example", UUID.randomUUID(), new FragmentedRange(0, 2), 1500, System.currentTimeMillis(),
                                       sk.getEncoded(), iv.getIV()));
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
                dos.write(new byte[]{0, 0, 0});
                dos.writeInt(1024);
                dos.writeLong(2048);
                Cipher fileCipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
                fileCipher.init(Cipher.ENCRYPT_MODE, sk, iv);
                try (CipherOutputStream cos = new CipherOutputStream(new NonClosableOutputStream(dos), fileCipher))
                {
                    cos.write(new byte[1500]);
                }
                byte[] filler = new byte[548];
                r.nextBytes(filler);
                dos.write(filler);
                dos.writeInt(isw.toString().length());
                dos.writeBytes(isw.toString());
                dos.writeInt(asw.toString().length());
                dos.writeBytes(asw.toString());
            }
        }

    }
}
