/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.imagereaders;

import java.awt.image.BufferedImage;
import java.io.File;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;

/**
 * Abstract class that facilitates the implementations of {@link IMetaDataAwareImageReader}.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractImageReader implements IImageReader
{
    private final String libraryName;

    private final String readerName;

    public AbstractImageReader(String libraryName, String readerName)
    {
        this.libraryName = libraryName;
        this.readerName = readerName;
    }

    public String getLibraryName()
    {
        return libraryName;
    }

    public String getName()
    {
        return readerName;
    }

    public BufferedImage readImage(File file, IReadParams params) throws IOExceptionUnchecked
    {
        IRandomAccessFile raf = new RandomAccessFileImpl(file, "r");
        return readImage(raf, params);
    }

    public BufferedImage readImage(byte[] bytes, IReadParams params)
    {
        IRandomAccessFile raf = new ByteBufferRandomAccessFile(bytes);
        return readImage(raf, params);
    }

}
