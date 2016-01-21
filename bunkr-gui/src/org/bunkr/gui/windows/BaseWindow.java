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

package org.bunkr.gui.windows;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.bunkr.core.Resources;

import java.io.IOException;

/**
 * Created At: 2015-12-24
 */
public abstract class BaseWindow
{
    protected final String cssCommon;

    private final Stage stage;
    private Scene scene;
    private Parent rootLayout;

    public BaseWindow(Stage container) throws IOException
    {
        this.stage = container;
        this.cssCommon = Resources.getExternalPath("/resources/css/common.css");
    }

    public BaseWindow() throws IOException
    {
        this(new Stage());
    }

    public void initialise()
    {
        this.initControls();
        this.rootLayout = this.initLayout();
        this.bindEvents();
        this.applyStyling();
        this.scene = this.initScene();
    }

    public abstract void initControls();
    public abstract Parent initLayout();
    public abstract void bindEvents();
    public abstract void applyStyling();
    public abstract Scene initScene();

    public Stage getStage()
    {
        return stage;
    }

    public Scene getScene()
    {
        return scene;
    }

    public Parent getRootLayout()
    {
        return rootLayout;
    }
}