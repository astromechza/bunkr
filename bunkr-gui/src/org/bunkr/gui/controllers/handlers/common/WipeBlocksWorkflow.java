package org.bunkr.gui.controllers.handlers.common;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.operations.WipeBlocksOp;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

/**
 * Created At: 2016-10-09
 */
public class WipeBlocksWorkflow
{
    public static void wipe(FragmentedRange wipeblocks, ArchiveInfoContext archive)
    {
        // now attempt wipe of those blocks if required
        if (!wipeblocks.isEmpty())
        {
            if (QuickDialogs.confirm("Do you want to securely wipe the data blocks used by the file you deleted?"))
            {
                WipeBlocksOp op = new WipeBlocksOp(archive.filePath, archive.getBlockSize(), wipeblocks, true);
                ProgressTask<Void> progressTask = new ProgressTask<Void>()
                {
                    @Override
                    protected Void innerCall() throws Exception
                    {
                        this.updateMessage("Wiping blocks");
                        op.setProgressUpdate(o -> this.updateProgress(o.getBlocksWiped(), o.getTotalBlocks()));
                        op.run();
                        return null;
                    }

                    @Override
                    protected void failed()
                    {
                        QuickDialogs.exception(this.getException());
                    }
                };

                ProgressDialog pd = new ProgressDialog(progressTask);
                pd.setHeaderText(String.format("Wiping %d data blocks ...", wipeblocks.size()));
                Thread task = new Thread(progressTask);
                task.setDaemon(true);
                task.start();
            }
        }
    }
}
