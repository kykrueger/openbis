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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvToCanonicalFeatureVector.CsvToCanonicalFeatureVectorConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CsvToCanonicalFeatureVectorTest extends AssertJUnit
{
    @Test
    public void testConversion() throws IOException
    {
        CsvToCanonicalFeatureVectorConfiguration config =
                new CsvToCanonicalFeatureVectorConfiguration("WellName", "WellName");
        CsvToCanonicalFeatureVector converter =
                new CsvToCanonicalFeatureVector(getDatasetFileLines(), config, 16, 24);
        ArrayList<CanonicalFeatureVector> fvs = converter.convert();
        // Not all the columns contain numerical data
        assertEquals(16, fvs.size());
        // Check total cells feature
        CanonicalFeatureVector totalCells = fvs.get(0);
        ImgFeatureDefDTO def = totalCells.getFeatureDef();
        assertEquals("TotalCells", def.getLabel());
        assertEquals(1, totalCells.getValues().size());
        ImgFeatureValuesDTO values = totalCells.getValues().get(0);
        PlateFeatureValues darr = values.getValues();
        assertTrue("Dimensions are " + darr.getGeometry().toPlateGeometryStr(),
                Geometry.GEOMETRY_384_16X24.equals(darr.getGeometry()));
        assertEquals(2825.0f, darr.getForWellLocation(1, 1));
        assertEquals(5544.0f, darr.getForWellLocation(1, 2));
        assertEquals(5701.0f, darr.getForWellLocation(2, 1));
        // Check InfectionIndex
        CanonicalFeatureVector infectionIndex = fvs.get(2);
        def = infectionIndex.getFeatureDef();
        assertEquals("InfectionIndex", def.getLabel());
        assertEquals(1, infectionIndex.getValues().size());
        values = infectionIndex.getValues().get(0);
        darr = values.getValues();
        assertTrue("Dimensions are " + darr.getGeometry().toPlateGeometryStr(),
                Geometry.GEOMETRY_384_16X24.equals(darr.getGeometry()));
        assertEquals(0.009558f, darr.getForWellLocation(1, 1));
        assertEquals(0.037157f, darr.getForWellLocation(1, 2));
        assertEquals(0.001052f, darr.getForWellLocation(2, 1));
    }

    /**
     * Return the tabular data as a DatasetFileLines.
     */
    protected DatasetFileLines getDatasetFileLines() throws IOException
    {
        File file =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/graph/CP037-1df.csv");
        CsvReader reader = getCsvReader(file);
        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }

        return new DatasetFileLines(file, "test", lines);

    }

    /**
     * Get a CsvReader for parsing a tabular data file.
     */
    protected CsvReader getCsvReader(File file) throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(';');
        csvReader.setSkipEmptyRecords(true);
        csvReader.setUseComments(true);
        csvReader.setComment('#');

        return csvReader;
    }
}
