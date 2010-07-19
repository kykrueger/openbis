/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.FileTypeExtractor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * The extractor that recognizes following data set types:
 * <ul>
 * <li>SEQ_FILE -- for a single file with one of extensions: <b>gb</b>, <b>fasta</b>, <b>xdna</b>
 * <li>RAW_DATA -- for a single file with <b>ab1</b> extension
 * <li>VERIFICATION -- for all other single files
 * <li>UNKNOWN -- for directories (they can contain files with arbitrary data)
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class PlasmidTypeExtractor extends FileTypeExtractor implements ITypeExtractor
{

    public PlasmidTypeExtractor(final Properties properties)
    {
        super(properties);
        DataSetTypeOracle.initializeMapping(properties);
    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        final String code =
                DataSetTypeOracle.extractDataSetTypeInfo(incomingDataSetPath).getDataSetTypeCode();
        return new DataSetType(code);
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return DataSetTypeOracle.extractDataSetTypeInfo(incomingDataSetPath).isMeasured();
    }

}
