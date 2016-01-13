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

package org.bunkr.gui.dialogs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * This progress dialog takes inspiration from the one provided by ControlsFX. The ControlsFX one is great but lacks
 * certain features we need and it seems silly bringing in a large dependency just for one class.
 *
 * Creator: benmeier
 * Created At: 2016-01-13
 */
public class ProgressDialog extends Dialog<Void>
{
    public ProgressDialog(final Worker<?> worker)
    {
        if (worker == null) return;
        if (worker.getState() == State.CANCELLED || worker.getState() == State.FAILED || worker.getState() == State.SUCCEEDED)
        {
            return;
        }
        setResultConverter(dialogButton -> null);

        final DialogPane dialogPane = getDialogPane();

        setTitle("Progress");
        dialogPane.setHeaderText("Operation in Progress");

        final Label progressMessage = new Label();
        progressMessage.textProperty().bind(worker.messageProperty());

        final WorkerProgressPane content = new WorkerProgressPane(this);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setWorker(worker);

        VBox vbox = new VBox(10, progressMessage, content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(300, 100);
        /**
         * The content Text cannot be set before the constructor and since we
         * set a Content Node, the contentText will not be shown. If we want to
         * let the user display a content text, we must recreate it.
         */
        Label contentText = new Label();
        contentText.setWrapText(true);
        vbox.getChildren().add(0, contentText);
        contentText.textProperty().bind(dialogPane.contentTextProperty());
        dialogPane.setContent(vbox);
    }

    /**
     * The WorkerProgressPane takes a {@link Dialog} and a {@link Worker}
     * and links them together so the dialog is shown or hidden depending
     * on the state of the worker.  The WorkerProgressPane also includes
     * a progress bar that is automatically bound to the progress property
     * of the worker.  The way in which the WorkerProgressPane shows and
     * hides its worker's dialog is consistent with the expected behavior
     * for showWorkerProgress(Worker).
     */
    private static class WorkerProgressPane extends Region
    {
        private Worker<?> worker;

        private boolean dialogVisible = false;
        private boolean cancelDialogShow = false;

        private ChangeListener<Worker.State> stateListener = (observable, old, value) ->
        {
            switch(value)
            {
                case CANCELLED:
                case FAILED:
                case SUCCEEDED:
                    if (! dialogVisible)
                    {
                        cancelDialogShow = true;
                        end();
                    }
                    else if (old == State.SCHEDULED || old == State.RUNNING)
                    {
                        end();
                    }
                    break;
                case SCHEDULED:
                    begin();
                    break;
                default: //no-op
            }
        };

        public final void setWorker(final Worker<?> newWorker)
        {
            if (newWorker != worker)
            {
                if (worker != null)
                {
                    worker.stateProperty().removeListener(stateListener);
                    end();
                }

                worker = newWorker;

                if (newWorker != null)
                {
                    newWorker.stateProperty().addListener(stateListener);
                    if (newWorker.getState() == Worker.State.RUNNING || newWorker.getState() == Worker.State.SCHEDULED)
                    {
                        // It is already running
                        begin();
                    }
                }
            }
        }

        private final ProgressDialog dialog;
        private final ProgressBar progressBar;

        public WorkerProgressPane(ProgressDialog dialog)
        {
            this.dialog = dialog;

            this.progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(progressBar);

            if (worker != null)
            {
                progressBar.progressProperty().bind(worker.progressProperty());
            }
        }

        private void begin()
        {
            cancelDialogShow = false;

            Platform.runLater(() -> {
                if (! cancelDialogShow)
                {
                    progressBar.progressProperty().bind(worker.progressProperty());
                    dialogVisible = true;
                    dialog.show();
                }
            });
        }

        private void end()
        {
            progressBar.progressProperty().unbind();
            dialogVisible = false;
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes().add(ButtonType.CANCEL);
            dialog.hide();
            dialogPane.getButtonTypes().remove(ButtonType.CANCEL);
        }

        @Override
        protected void layoutChildren()
        {
            Insets insets = getInsets();
            double w = getWidth() - insets.getLeft() - insets.getRight();
            double h = getHeight() - insets.getTop() - insets.getBottom();

            double prefH = progressBar.prefHeight(-1);
            double x = insets.getLeft() + (w - w) / 2.0;
            double y = insets.getTop() + (h - prefH) / 2.0;

            progressBar.resizeRelocate(x, y, w, prefH);
        }
    }
}
