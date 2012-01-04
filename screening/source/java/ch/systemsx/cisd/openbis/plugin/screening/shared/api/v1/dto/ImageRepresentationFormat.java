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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * An image representation format is made up of its dimensions, bit depth, and file format.
 * 
 * @since 1.10
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageRepresentationFormat implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String dataSetCode;
    
    private final long id;
    
    private boolean original;

    private Integer width;

    private Integer height;

    private Integer colorDepth;

    private String fileType;

    /**
     * Constructor.
     * 
     * @param dataSetCode 
     * @param id 
     * @param original
     * @param width
     * @param height
     * @param colorDepth
     * @param fileType
     */
    public ImageRepresentationFormat(String dataSetCode, long id, boolean original, Integer width, Integer height,
            Integer colorDepth, String fileType)
    {
        super();
        this.dataSetCode = dataSetCode;
        this.id = id;
        this.original = original;
        this.width = width;
        this.height = height;
        this.colorDepth = colorDepth;
        this.fileType = fileType;
    }

    /**
     * Returns the data set code to which this image representation format belongs.
     */
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    /**
     * Return the ID of this image representation format. 
     */
    public long getId()
    {
        return id;
    }

    /**
     * @return True if the format is the original format of the underlying image.
     */
    public boolean isOriginal()
    {
        return original;
    }

    /**
     * @return The width of the image.
     */
    public Integer getWidth()
    {
        return width;
    }

    /**
     * @return The height of the image.
     */
    public Integer getHeight()
    {
        return height;
    }

    /**
     * @return The number of bits of color of the image.
     */
    public Integer getColorDepth()
    {
        return colorDepth;
    }

    /**
     * @return The file type of the image.
     */
    public String getFileType()
    {
        return fileType;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(original);
        builder.append(width);
        builder.append(height);
        builder.append(colorDepth);
        builder.append(fileType);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        ImageRepresentationFormat other = (ImageRepresentationFormat) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(original, other.original);
        builder.append(width, other.width);
        builder.append(height, other.height);
        builder.append(colorDepth, other.colorDepth);
        builder.append(fileType, other.fileType);
        return builder.isEquals();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(original);
        builder.append(width);
        builder.append(height);
        builder.append(colorDepth);
        builder.append(fileType);
        return builder.toString();
    }

}
