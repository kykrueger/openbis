package ch.systemsx.cisd.common.fileconverter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

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

/**
 * A class that performs conversion of a tiff file to a png file with optional page extraction and
 * transparency conversion using the ImageMagick <code>convert</code> utility.
 * 
 * @author Bernd Rinn
 */
public class ImageMagickTiffToPngConverter extends AbstractImageMagickConvertImageFileConverter
        implements IFileConversionStrategy
{
    private final String transparentColorOrNull;

    private final boolean replaceFile;

    private final int page;

    public ImageMagickTiffToPngConverter(String transparentColorOrNull, int page,
            boolean replaceFile)
    {
        super(LogFactory.getLogger(LogCategory.MACHINE, ImageMagickTiffToPngConverter.class),
                LogFactory.getLogger(LogCategory.OPERATION, ImageMagickTiffToPngConverter.class));
        this.transparentColorOrNull = transparentColorOrNull;
        this.page = page;
        this.replaceFile = replaceFile;
    }

    @Override
    protected List<String> getCommandLine(File inFile, File outFile)
    {
        if (StringUtils.isEmpty(transparentColorOrNull))
        {

            return Arrays.asList("-transparent", transparentColorOrNull, inFile.getAbsolutePath()
                    + "[" + page + "]", outFile.getAbsolutePath());
        } else
        {
            return Arrays.asList(inFile.getAbsolutePath() + "[" + page + "]",
                    outFile.getAbsolutePath());
        }
    }

    //
    // IFileConversionStrategy
    //

    public File tryCheckConvert(File inFile)
    {
        final String ext = FilenameUtils.getExtension(inFile.getName()).toLowerCase();
        if ("tiff".equals(ext) || "tif".equals(ext))
        {
            return new File(inFile.getParent(), FilenameUtils.getBaseName(inFile.getName())
                    + ".png");
        } else
        {
            return null;
        }
    }

    public boolean deleteOriginalFile()
    {
        return replaceFile;
    }

    public IFileConversionMethod getConverter()
    {
        return this;
    }

}
