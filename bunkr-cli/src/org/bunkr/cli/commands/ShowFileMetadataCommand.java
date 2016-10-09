package org.bunkr.cli.commands;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.cli.CLI;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Formatters;

import javax.xml.bind.DatatypeConverter;

/**
 * Created At: 2016-10-09
 */
public class ShowFileMetadataCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("output the metadata for the file at the given path");
        target.description(
                "This the metadata associated with the given file. This includes all information except for the " +
                "actual file contents. It will contain a representation of the symmetric key, so dont use it in " +
                "sensitive contexts."
        );
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("directory path to list");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));

        if (t.isAFile())
        {
            FileInventoryItem file = (FileInventoryItem) t;

            System.out.printf("Name:                  %s\n", file.getName());
            System.out.printf("UUID:                  %s\n", file.getUuid());
            System.out.printf("Actual size:           %d (%s)\n", file.getActualSize(), Formatters.formatBytes(file.getActualSize()));
            System.out.printf("Size on disk:          %d (%s)\n", file.getSizeOnDisk(), Formatters.formatBytes(file.getSizeOnDisk()));
            float compression = 1 - (file.getSizeOnDisk()) / (float)(file.getActualSize());
            System.out.printf("Compression saving:    %.2f%%\n", compression * 100);
            System.out.printf("Modified at:           %s\n", Formatters.formatPrettyDate(file.getModifiedAt()));
            System.out.printf("Media type:            %s\n", file.getMediaType());
            System.out.printf("Integrity hash:        %s\n", DatatypeConverter.printHexBinary(file.getIntegrityHash()));
            System.out.printf("Encryption algorithm:  %s\n", file.getEncryptionAlgorithm());
            System.out.printf("Encryption data:       %s\n", DatatypeConverter.printHexBinary(file.getEncryptionData()));
            System.out.printf("Block count:           %d\n", file.getBlocks().size());
            System.out.printf("Block ranges:          ");
            BooleanProperty first = new SimpleBooleanProperty(true);
            file.getBlocks().iteratePairs().forEachRemaining(p -> {
                if (first.getValue())
                    first.set(false);
                else
                    System.out.printf(", ");
                if (p.getValue() > 1)
                {
                    System.out.printf("%d-%d", p.getKey(), p.getKey() + p.getValue());
                }
                else
                {
                    System.out.printf("%d", p.getKey());
                }
            });
            System.out.println();
        }
        else
        {
            throw new TraversalException("'%s' is not a file", args.getString(ARG_PATH));
        }
    }
}
