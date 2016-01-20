/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.bunkr.cli.CLI;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Formatters;

import java.util.LinkedHashMap;

/**
 * Creator: benmeier
 * Created At: 2016-01-10
 */
public class ChangeSecurityCommand implements ICLICommand
{
    public static final String ARG_ARCHIVE_SECURITY = "algorithm";
    public static final String ARG_NEW_PASSWORD_FILE = "newpasswordfile";
    public static final String ARG_FILE_SECURITY = "filesecurity";
    public static final String ARG_INV_SECURITY = "inventorysecurity";
    public static final String ARG_ITERATIONS_TIME = "pbkdf2iterationstime";
    public static final String ARG_MEMORY_USAGE = "scryptmemoryusage";

    private final LinkedHashMap<String, Integer> pbkdf2IterTimeChoices;
    private final LinkedHashMap<String, Integer> scryptNChoices;

    public ChangeSecurityCommand()
    {
        this.pbkdf2IterTimeChoices = new LinkedHashMap<>();
        for (Integer ms : PBKDF2Descriptor.SUGGESTED_ITERATION_TIME_LIST)
        {
            this.pbkdf2IterTimeChoices.put(String.format("%.1fs", (float) ms / 1000), ms);
        }
        this.scryptNChoices = new LinkedHashMap<>();
        for (Integer n : ScryptDescriptor.SUGGESTED_SCRYPT_N_LIST)
        {
            this.scryptNChoices.put(Formatters.formatBytes(ScryptDescriptor.calculateMemoryUsage(n)) + "B", n);
        }
    }

    @Override
    public void buildParser(Subparser target)
    {
        target.help("change the security settings for the archive");

        Subparsers securityType = target.addSubparsers().dest(ARG_ARCHIVE_SECURITY);
        securityType.addParser(PlaintextDescriptor.IDENTIFIER.toLowerCase());

        Subparser p1 = securityType.addParser(PBKDF2Descriptor.IDENTIFIER.toLowerCase());
        p1.addArgument("new-password-file")
                .dest(ARG_NEW_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead().acceptSystemIn())
                .help("read the new archive password from the given file or '-' for stdin");
        p1.addArgument("--file-security")
                .dest(ARG_FILE_SECURITY)
                .setDefault(Algorithms.Encryption.AES256_CTR)
                .choices(Algorithms.Encryption.AES128_CTR, Algorithms.Encryption.AES256_CTR, Algorithms.Encryption.TWOFISH128_CTR, Algorithms.Encryption.TWOFISH256_CTR)
                .type(Algorithms.Encryption.class)
                .help("set the encryption used on files");
        p1.addArgument("--inventory-security")
                .dest(ARG_INV_SECURITY)
                .setDefault(Algorithms.Encryption.AES256_CTR)
                .choices(Algorithms.Encryption.AES128_CTR, Algorithms.Encryption.AES256_CTR,
                         Algorithms.Encryption.TWOFISH128_CTR, Algorithms.Encryption.TWOFISH256_CTR)
                .type(Algorithms.Encryption.class)
                .help("set the encryption used on the hierarchy metadata");
        p1.addArgument("--iterations-time")
                .dest(ARG_ITERATIONS_TIME)
                .setDefault(pbkdf2IterTimeChoices.keySet().iterator().next())
                .choices(pbkdf2IterTimeChoices.keySet())
                .help("calculate the number of sha operations based on a time limit");

        Subparser p2 = securityType.addParser(ScryptDescriptor.IDENTIFIER.toLowerCase());
        p2.addArgument(ARG_NEW_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead().acceptSystemIn())
                .help("read the new archive password from the given file or '-' for stdin");
        p2.addArgument("--file-security")
                .dest(ARG_FILE_SECURITY)
                .setDefault(Algorithms.Encryption.AES256_CTR)
                .choices(Algorithms.Encryption.AES128_CTR, Algorithms.Encryption.AES256_CTR, Algorithms.Encryption.TWOFISH128_CTR, Algorithms.Encryption.TWOFISH256_CTR)
                .type(Algorithms.Encryption.class)
                .help("set the encryption used on files");
        p2.addArgument("--inventory-security")
                .dest(ARG_INV_SECURITY)
                .setDefault(Algorithms.Encryption.AES256_CTR)
                .choices(Algorithms.Encryption.AES128_CTR, Algorithms.Encryption.AES256_CTR, Algorithms.Encryption.TWOFISH128_CTR, Algorithms.Encryption.TWOFISH256_CTR)
                .type(Algorithms.Encryption.class)
                .help("set the encryption used on the hierarchy metadata");
        p2.addArgument("--memory-use")
                .dest(ARG_MEMORY_USAGE)
                .setDefault(scryptNChoices.keySet().iterator().next())
                .choices(scryptNChoices.keySet())
                .help("set the scrypt N parameter");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        // build security provider
        UserSecurityProvider usp = new UserSecurityProvider(
                makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE), "Enter current archive password:")
        );
        // open archive file
        ArchiveInfoContext context = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        String before = String.format("Archive Security: %s, File Security: %s",
                                      context.getDescriptor().getIdentifier(),
                                      context.getInventory().getDefaultEncryption());

        switch ((String) args.get(ARG_ARCHIVE_SECURITY))
        {
            case PlaintextDescriptor.IDENTIFIER:
                context.setDescriptor(new PlaintextDescriptor());
                context.getInventory().setDefaultEncryption(Algorithms.Encryption.NONE);
                break;
            case PBKDF2Descriptor.IDENTIFIER:
                usp.setProvider(makeCLIPasswordProvider(args.get(ARG_NEW_PASSWORD_FILE), "Enter new password for pbkdf2:"));
                applyPBKDF2SecuritySettings(context, args);
                break;
            case ScryptDescriptor.IDENTIFIER:
                usp.setProvider(makeCLIPasswordProvider(args.get(ARG_NEW_PASSWORD_FILE), "Enter new password for scrypt:"));
                applyScryptSecuritySettings(context, args);
                break;
            default:
                return;
        }

        String after = String.format("Archive Security: %s, File Security: %s",
                                     context.getDescriptor().getIdentifier(),
                                     context.getInventory().getDefaultEncryption());

        MetadataWriter.write(context, usp);
        System.out.println("Successfully changed security settings for achive.");
        System.out.println(String.format("Before: %s", before));
        System.out.println(String.format("After: %s", after));
    }

    public void applyPBKDF2SecuritySettings(ArchiveInfoContext archive, Namespace args)
    {
        archive.getInventory().setDefaultEncryption(args.get(ARG_FILE_SECURITY));
        int ms = pbkdf2IterTimeChoices.get((String) args.get(ARG_ITERATIONS_TIME));
        System.out.println(String.format("Calculating pbkdf2 rounds for %s milliseconds...", ms));
        int pbkdf2Rounds = PBKDF2Descriptor.calculateRounds(ms);
        System.out.println(String.format("Got %d rounds.", pbkdf2Rounds));
        archive.setDescriptor(PBKDF2Descriptor.make(args.get(ARG_INV_SECURITY), pbkdf2Rounds));
    }

    public void applyScryptSecuritySettings(ArchiveInfoContext archive, Namespace args)
    {
        archive.getInventory().setDefaultEncryption(args.get(ARG_FILE_SECURITY));
        archive.setDescriptor(ScryptDescriptor.make(
                args.get(ARG_INV_SECURITY), scryptNChoices.get((String) args.get(ARG_MEMORY_USAGE))
        ));
    }
}
