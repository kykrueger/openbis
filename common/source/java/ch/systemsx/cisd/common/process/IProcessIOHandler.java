package ch.systemsx.cisd.common.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Role for handling the process I/O.
 * <p>
 * Recommended pattern for output-only processes in
 * {@link IProcessIOHandler#handle(AtomicBoolean, OutputStream, InputStream, InputStream)}:
 * 
 * <pre>
 * while (processRunning.get())
 * {
 *     ProcessExecutionHelper.readBytesIfAvailable(stdout, out, buf, -1, false);
 *     ProcessExecutionHelper.readTextIfAvailable(bufStderr, errLines, false);
 * }
 * </pre>
 * 
 * @author Bernd Rinn
 */
public interface IProcessIOHandler
{
    /**
     * Method that gets the process' <code>stdin</code>, <code>stdout</code> and
     * <code>stderr</code> and is expected to handlt the I/O of the process. If
     * <var>processRunning</var> is <code>false</code>, the process has been finished or
     * terminated.
     */
    public void handle(AtomicBoolean processRunning, OutputStream stdin, InputStream stdout,
            InputStream stderr) throws IOException;
}