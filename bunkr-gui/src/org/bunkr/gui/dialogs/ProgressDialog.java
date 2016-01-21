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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * This progress dialog takes inspiration from the one provided by ControlsFX.
 *
 * Created At: 2016-01-13
 */
public class ProgressDialog extends Dialog<Void>
{
    Worker<?> worker;
    Label progressMessage, contentText;
    Button cancelButton;
    WorkerProgressPane progressPane;

    public ProgressDialog(final Worker<?> worker)
    {
        if (worker == null) return;
        if (worker.getState() == State.CANCELLED || worker.getState() == State.FAILED || worker.getState() == State.SUCCEEDED)
        {
            return;
        }
        this.worker = worker;

        setResultConverter(dialogButton -> null);

        setTitle("Progress");
        getDialogPane().setHeaderText("Operation in Progress");

        createControls();
        createLayout();
        bindEvents();
    }

    private void createControls()
    {
        contentText = new Label();
        contentText.setWrapText(true);
        contentText.setManaged(false);
        contentText.setVisible(false);
        progressMessage = new Label();
        progressPane = new WorkerProgressPane(this);
        progressPane.setWorker(worker);
        cancelButton = new Button("Cancel");
    }

    private void createLayout()
    {
        this.getDialogPane().setPrefWidth(480);
        progressPane.setMaxWidth(Double.MAX_VALUE);
        HBox buttonBox = new HBox(10, cancelButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        VBox vbox = new VBox(10, contentText, progressMessage, progressPane, buttonBox);
        vbox.setPadding(new Insets(10, 10, 0, 10));
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(buttonBox, Priority.ALWAYS);
        getDialogPane().setContent(vbox);
    }

    private void bindEvents()
    {
        progressMessage.textProperty().bind(worker.messageProperty());
        contentText.textProperty().bind(getDialogPane().contentTextProperty());
        cancelButton.setOnAction(event -> worker.cancel());

        contentText.textProperty().addListener((observable, oldValue, newValue) -> {
            contentText.setManaged(true);
            contentText.setVisible(true);
        });
    }

    private static class WorkerProgressPane extends Region
    {
        private final ProgressDialog dialog;
        private final ProgressBar progressBar;
        private Worker<?> worker;
        private boolean dialogVisible = false;
        private boolean cancelDialogShow = false;

        public WorkerProgressPane(ProgressDialog dialog)
        {
            this.dialog = dialog;

            this.progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(progressBar);
        }

        private final ChangeListener<Worker.State> stateListener = (observable, old, value) ->
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
            double x = insets.getLeft() + (w - w) / 2;
            double y = insets.getTop() + (h - prefH) / 2;

            progressBar.resizeRelocate(x, y, w, prefH);
        }
    }
}
