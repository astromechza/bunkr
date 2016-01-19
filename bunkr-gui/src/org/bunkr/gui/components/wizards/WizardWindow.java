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

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import org.bunkr.core.Resources;
import org.bunkr.gui.windows.BaseWindow;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2016-01-18
 */
public class WizardWindow extends BaseWindow
{
    private final String cssPath;
    private final String title;

    private Label pageLabel;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button cancelWizardButton;
    private Button finishWizardButton;
    private BorderPane centerPanel;

    private List<Pane> pages;
    private int currentPage = -1;

    public WizardWindow(String title, Pane... pages) throws IOException
    {
        super();
        this.title = title;
        this.pages = Arrays.asList(pages);
        this.cssPath = Resources.getExternalPath("/resources/css/wizard.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.pageLabel = new Label(title);
        this.previousPageButton = new Button("Previous");
        this.nextPageButton = new Button("Next");
        this.cancelWizardButton = new Button("Cancel");
        this.cancelWizardButton.setFocusTraversable(false);
        this.finishWizardButton = new Button("Finish");
        this.finishWizardButton.setFocusTraversable(false);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane rootPane = new BorderPane();
        rootPane.setPadding(new Insets(10));
        rootPane.setTop(pageLabel);
        this.centerPanel = new BorderPane();
        this.centerPanel.setMinWidth(400);
        this.centerPanel.setMinHeight(30);
        rootPane.setCenter(this.centerPanel);
        BorderPane.setMargin(this.centerPanel, new Insets(10, 0, 10, 0));
        rootPane.setBottom(
                new HBox(5, hExpander(), previousPageButton, nextPageButton, cancelWizardButton, finishWizardButton));
        return rootPane;
    }

    @Override
    public void bindEvents()
    {
        nextPageButton.setOnAction(e -> nextPage());
        previousPageButton.setOnAction(e -> previousPage());
        cancelWizardButton.setOnAction(e -> {
            if (cancel()) this.getStage().close();
        });

        finishWizardButton.setOnAction(e -> {
            if (finish()) this.getStage().close();
        });
    }

    @Override
    public void applyStyling()
    {
        this.pageLabel.getStyleClass().add("wizard-page-label");
        this.centerPanel.getStyleClass().add("wizard-center-pane");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().sizeToScene();
        this.getStage().setResizable(false);
        return scene;
    }

    public void setPages(Pane... pages)
    {
        this.pages = Arrays.asList(pages);
    }

    public void gotoPage(int i)
    {
        if (i < 0 || i >= pages.size()) return;

        currentPage = i;
        this.centerPanel.setCenter(pages.get(i));

        previousPageButton.setDisable(i < 1);
        nextPageButton.setDisable(i >= pages.size() - 1);

        pageLabel.setText(String.format("%s (%d / %d)", title, currentPage + 1, pages.size()));
        this.getStage().sizeToScene();
    }

    public void nextPage()
    {
        gotoPage(currentPage + 1);
    }

    public void previousPage()
    {
        gotoPage(currentPage - 1);
    }


    private Region hExpander()
    {
        Region e = new Region();
        HBox.setHgrow(e, Priority.ALWAYS);
        return e;
    }

    public boolean cancel()
    {
        // no op
        return true;
    }

    public boolean finish()
    {
        // no op
        return true;
    }
}
