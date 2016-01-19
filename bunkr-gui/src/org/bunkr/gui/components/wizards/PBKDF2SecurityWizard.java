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

package org.bunkr.gui.components.wizards;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.components.ComboBoxItem;
import org.bunkr.gui.components.wizards.common.AesKeyLengthWizardPanel;
import org.bunkr.gui.components.wizards.common.FileSecWizardPanel;
import org.bunkr.gui.components.wizards.common.PasswordWizardPanel;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2016-01-18
 */
public class PBKDF2SecurityWizard extends WizardWindow
{
    private final ArchiveInfoContext archive;
    private final UserSecurityProvider securityProvider;
    private final AesKeyLengthWizardPanel aesklpanel;
    private final IterationsWizardPanel pbkditerpanel;
    private final PasswordWizardPanel pwdpanel;
    private final FileSecWizardPanel filesecpanel;

    public PBKDF2SecurityWizard(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super("Configure PBKDF2 Security");
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.aesklpanel = new AesKeyLengthWizardPanel();
        this.pbkditerpanel = new IterationsWizardPanel();
        this.pwdpanel = new PasswordWizardPanel();
        this.filesecpanel = new FileSecWizardPanel();
        this.setPages(aesklpanel, pbkditerpanel, pwdpanel, filesecpanel);
        this.gotoPage(0);
    }

    @SuppressWarnings("unchecked")
    private static class IterationsWizardPanel extends VBox
    {
        protected ComboBox<ComboBoxItem<Integer, String>> timeComboBox = new ComboBox<>();
        public IterationsWizardPanel()
        {
            this.setSpacing(10);
            timeComboBox.getItems().addAll(
                    new ComboBoxItem<>(100, "100ms"),
                    new ComboBoxItem<>(500, "0.5s"),
                    new ComboBoxItem<>(1000, "1s"),
                    new ComboBoxItem<>(2000, "2s"),
                    new ComboBoxItem<>(3000, "3s"),
                    new ComboBoxItem<>(5000, "5s"),
                    new ComboBoxItem<>(10000, "10s")
            );
            timeComboBox.getSelectionModel().select(2);
            this.getChildren().add(new HBox(10, new Label("PBKDF2 Calculation Time:"), timeComboBox));
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
        int selectedTime = pbkditerpanel.timeComboBox.getSelectionModel().getSelectedItem().valueValue;

        Logging.info("Calculating how many SHA256 rounds we can do in %d millis.", selectedTime);
        HMac mac = new HMac(new SHA256Digest());
        byte[] state = new byte[mac.getMacSize()];
        long startTime = System.currentTimeMillis();
        int pbkdf2Iterations = 0;
        while((System.currentTimeMillis() - startTime) < selectedTime)
        {
            mac.update(state, 0, state.length);
            mac.doFinal(state, 0);
            pbkdf2Iterations++;
        }
        pbkdf2Iterations = Math.max(pbkdf2Iterations, PBKDF2Descriptor.MINIMUM_PBKD2_ITERS);
        Logging.info("Got %d", pbkdf2Iterations);

        // now the aes key length
        int aeskeylength = aesklpanel.getSelectedKeyLength();
        aeskeylength = Math.max(aeskeylength, PBKDF2Descriptor.MINIMUM_AES_KEY_LENGTH);

        try
        {
            // create new Descriptor
            IDescriptor newDescriptor = new PBKDF2Descriptor(aeskeylength, pbkdf2Iterations, new byte[PBKDF2Descriptor.SALT_LENGTH]);

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
