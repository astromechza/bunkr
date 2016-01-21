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
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.DisplayValuePair;
import org.bunkr.gui.wizards.common.FileSecWizardPanel;
import org.bunkr.gui.wizards.common.InventorySecWizardPanel;
import org.bunkr.gui.wizards.common.PasswordWizardPanel;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

/**
 * Created At: 2016-01-18
 */
public class PBKDF2SecurityWizard extends WizardWindow
{
    private final ArchiveInfoContext archive;
    private final UserSecurityProvider securityProvider;
    private final InventorySecWizardPanel invSecPanel;
    private final IterationsWizardPanel pbkditerpanel;
    private final PasswordWizardPanel pwdpanel;
    private final FileSecWizardPanel filesecpanel;

    public PBKDF2SecurityWizard(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super("Configure PBKDF2 Security");
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.invSecPanel = new InventorySecWizardPanel();
        this.pbkditerpanel = new IterationsWizardPanel();
        this.pwdpanel = new PasswordWizardPanel();
        this.filesecpanel = new FileSecWizardPanel();
        this.setPages(invSecPanel, pbkditerpanel, pwdpanel, filesecpanel);
        this.gotoPage(0);
    }

    @SuppressWarnings("unchecked")
    private static class IterationsWizardPanel extends VBox
    {
        private static final String DESCRIPTION_TEXT = "PBKDF2 uses many rounds of SHA256 to calculate the final " +
                "symmetric key. Pick the amount of time you'd like it to take to open the archive using your " +
                "current hardware.";
        protected final ComboBox<DisplayValuePair<Integer, String>> timeComboBox = new ComboBox<>();
        public IterationsWizardPanel()
        {
            this.setSpacing(10);
            Label descriptionLabel = new Label(DESCRIPTION_TEXT);
            descriptionLabel.setWrapText(true);
            this.getChildren().add(descriptionLabel);
            for (Integer time : PBKDF2Descriptor.SUGGESTED_ITERATION_TIME_LIST)
            {
                timeComboBox.getItems().add(new DisplayValuePair<>(time, String.format("%dms", time)));
            }
            timeComboBox.getSelectionModel().select(0);
            Label label = new Label("PBKDF2 Calculation Time:");
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

        // now the pbkdf2 iterations
        int selectedTime = pbkditerpanel.timeComboBox.getSelectionModel().getSelectedItem().value;
        int pbkdf2Iterations = PBKDF2Descriptor.calculateRounds(selectedTime);

        try
        {
            // create new Descriptor
            IDescriptor newDescriptor = new PBKDF2Descriptor(invSecPanel.getSelectedValue(), pbkdf2Iterations, new byte[PBKDF2Descriptor.SALT_LENGTH]);

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
