/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.datamover;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * Interface that represents a role that copies files and directories to another directory, usually on another file
 * system.
 * <p>
 * <i>Note: If the copier is terminated, the <var>destinationDirectory</var> is in an undefined state afterwards.</i>
 * 
 * @author Bernd Rinn
 */
public interface IPathCopier extends ITerminable, ISelfTestable
{

    /**
     * @return <code>true</code> if this copier supports explicit specifying a destination host, that is, if
     *         {@link #copy(File, File, String)} may be called.
     */
    public boolean supportsExplicitHost();
    
    /**
     * Must not be called if {@link #supportsExplicitHost()} returns <code>false</code>.
     * 
     * @return <code>true</code> iff <var>destinationDirectory</var> on host <var>destinationHost</var> exists.
     * @throws IllegalStateException If this copier does not support explicitely specifying a destination host, that is
     *             if {@link #supportsExplicitHost()} returns <code>false</code>.
     */
    public boolean exists(File destinationDirectory, String destinationHost);

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and
     *            writable. If <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    public Status copy(File sourcePath, File destinationDirectory);

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var> on <var>destinationHost</var>. Must not be called if
     * {@link #supportsExplicitHost()} returns <code>false</code>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and
     *            writable. If <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param destinationHost The host where the <var>destinationDirectory</var> resides.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     * @throws IllegalStateException If this copier does not support explicitely specifying a destination host, that is
     *             if {@link #supportsExplicitHost()} returns <code>false</code>.
     */
    public Status copy(File sourcePath, File destinationDirectory, String destinationHost);

}
