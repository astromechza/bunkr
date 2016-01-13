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

package org.bunkr.gui;

import javafx.concurrent.Task;
import org.bunkr.core.utils.Formatters;

/**
 * Creator: benmeier
 * Created At: 2016-01-13
 */
public abstract class ProgressTask<V> extends Task<V>
{
    private long startTime;
    private String currentStateMessage;

    @Override
    protected final V call() throws Exception
    {
        this.startTime = System.currentTimeMillis();
        return this.innerCall();
    }

    protected abstract V innerCall() throws Exception;

    @Override
    protected void updateMessage(String message)
    {
        this.currentStateMessage = message;
        super.updateMessage(message);
    }

    @Override
    protected void updateProgress(double done, double total)
    {
        if (done > 0)
        {
            long elapsed = System.currentTimeMillis() - this.startTime;
            double eta = elapsed * (total - done) / done;
            super.updateMessage(String.format(
                                       "%s (%s/s ETA: %s)",
                                       currentStateMessage,
                                       Formatters.formatPrettyInt((long) (1000 * done / elapsed)),
                                       Formatters.formatPrettyElapsed(eta)
                               )
            );
            super.updateProgress(done, total);
        }
    }
}
