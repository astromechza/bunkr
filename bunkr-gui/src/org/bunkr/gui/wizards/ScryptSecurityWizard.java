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

package org.bunkr.gui.wizards;

import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Formatters;
import org.bunkr.gui.components.ComboBoxItem;
import org.bunkr.gui.wizards.common.FileSecWizardPanel;
import org.bunkr.gui.wizards.common.InventorySecWizardPanel;
import org.bunkr.gui.wizards.common.PasswordWizardPanel;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2016-01-19
 */
public class ScryptSecurityWizard extends WizardWindow
{
    private final ArchiveInfoContext archive;
    private final UserSecurityProvider securityProvider;
    private final InventorySecWizardPanel invsecpanel;
    private final ScryptMemCostWizardPanel memusepanel;
    private final PasswordWizardPanel pwdpanel;
    private final FileSecWizardPanel filesecpanel;

    public ScryptSecurityWizard(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super("Configure Scrypt Security");
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.invsecpanel = new InventorySecWizardPanel();
        this.memusepanel = new ScryptMemCostWizardPanel();
        this.pwdpanel = new PasswordWizardPanel();
        this.filesecpanel = new FileSecWizardPanel();
        this.setPages(invsecpanel, memusepanel, pwdpanel, filesecpanel);
        this.gotoPage(0);
    }

    @SuppressWarnings("unchecked")
    private static class ScryptMemCostWizardPanel extends VBox
    {
        private static final String DESCRIPTION_TEXT = "Scrypt gets it's strength from the memory required to " +
                "process. Select a larger value for a larger memory requirement.";
        protected ComboBox<ComboBoxItem<Integer, String>> timeComboBox = new ComboBox<>();
        public ScryptMemCostWizardPanel()
        {
            this.setSpacing(10);
            Label descriptionLabel = new Label(DESCRIPTION_TEXT);
            descriptionLabel.setWrapText(true);
            this.getChildren().add(descriptionLabel);
            int n = ScryptDescriptor.MINIMUM_SCRYPT_N;
            for (int i = 0; i < 8; i++)
            {
                long memusage = (128L * (long) ScryptDescriptor.DEFAULT_SCRYPT_R * n) + (128L * (long) ScryptDescriptor.DEFAULT_SCRYPT_R * (long) ScryptDescriptor.DEFAULT_SCRYPT_P);
                timeComboBox.getItems().add(new ComboBoxItem<>(n, Formatters.formatBytes(memusage) + "B"));
                n <<= 1;
            }
            timeComboBox.getSelectionModel().select(0);
            Label label = new Label("Scrypt Memory Usage:");
            label.setMaxHeight(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(new HBox(10, label, timeComboBox));
        }
    }

    @Override
    public boolean finish()
    {
        // Validation of choices here

        // first the password
        try
        {
            pwdpanel.getPasswordValue();
        }
        catch (IllegalPasswordException e)
        {
            gotoPage(2);
            QuickDialogs.error("Password does not meet requirements: %s", e.getMessage());
            return false;
        }

        // now the scrypt memusage
        int scryptN = memusepanel.timeComboBox.getValue().valueValue;
        scryptN = Math.max(scryptN, ScryptDescriptor.MINIMUM_SCRYPT_N);

        try
        {
            // create new Descriptor
            IDescriptor newDescriptor = new ScryptDescriptor(invsecpanel.getSelectedValue(), scryptN, new byte[ScryptDescriptor.SALT_LENGTH], ScryptDescriptor.DEFAULT_SCRYPT_R, ScryptDescriptor.DEFAULT_SCRYPT_P);

            // create new security
            securityProvider.getProvider().setArchivePassword(pwdpanel.getPasswordValue().getBytes());

            // set on archive
            archive.setDescriptor(newDescriptor);
            archive.getInventory().setDefaultEncryption(filesecpanel.getSelectedValue());

            // save!
            MetadataWriter.write(archive, securityProvider);
        }
        catch (BaseBunkrException | IOException e)
        {
            QuickDialogs.exception(e);
            return false;
        }
        return true;
    }
}
