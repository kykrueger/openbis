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

package ch.systemsx.cisd.bds.hcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IFormatParameterFactory;
import ch.systemsx.cisd.bds.Version;

/**
 * <code>Format</code> extension for <i>HCS (High-Content Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFormatV1_0 extends Format
{

    public static final String FORMAT_CODE = "HCS_IMAGE";

    /**
     * Flag ({@link Boolean#TRUE} or {@link Boolean#FALSE}) specifying whether the data directory contains the original data or not.
     */
    public final static String CONTAINS_ORIGINAL_DATA = "contains_original_data";

    /**
     * Unique identifier when used as {@link FormatParameter}.
     */
    public final static String NUMBER_OF_CHANNELS = "number_of_channels";

    /**
     * Boolean flag specifying whether the items in the incoming folder are just a symbolic links to the original data. In this case we do not move
     * any data. Instead we create symbolic link to original data which points to the same place as the link in incoming directory.<br>
     * If this parameter is not present we assume that it has false value.
     */
    public final static String IS_INCOMING_SYMBOLIC_LINK = "incoming_items_are_symbolic_link";

    /**
     * Extension of the images files in the standard folder.<br>
     * If this parameter is not present we assume that it has {@link #DEFAULT_FILE_EXTENSION} value.
     */
    public final static String IMAGE_FILE_EXTENSION = "file_extension";

    public final static String DEFAULT_FILE_EXTENSION = "tiff";

    /**
     * The format parameters that must be defined so that this implementation is able to work properly.
     * <p>
     * These parameters are located in <code>metadata/parameters</code>.
     * </p>
     */
    private final static String[] MANDATORY_FORMAT_PARAMETERS = new String[]
    { WellGeometry.WELL_GEOMETRY, NUMBER_OF_CHANNELS, CONTAINS_ORIGINAL_DATA };

    private final static String[] OPTIONAL_FORMAT_PARAMETERS = new String[]
    { IS_INCOMING_SYMBOLIC_LINK, IMAGE_FILE_EXTENSION };

    /**
     * The one and only one instance.
     */
    public static final Format HCS_IMAGE_1_0 = new HCSImageFormatV1_0();

    private HCSImageFormatV1_0()
    {
        super(FORMAT_CODE, new Version(1, 0), null);
    }

    //
    // Format
    //

    @Override
    public final List<String> getMandatoryParameterNames()
    {
        List<String> result = new ArrayList<String>();
        result.addAll(Arrays.asList(MANDATORY_FORMAT_PARAMETERS));
        result.addAll(super.getMandatoryParameterNames());
        return result;
    }

    @Override
    public List<String> getOptionalParameterNames()
    {
        List<String> result = new ArrayList<String>();
        result.addAll(Arrays.asList(OPTIONAL_FORMAT_PARAMETERS));
        result.addAll(super.getOptionalParameterNames());
        return result;
    }

    @Override
    public final IFormatParameterFactory getFormatParameterFactory()
    {
        return FormatParameterFactory.getInstance();
    }

}
