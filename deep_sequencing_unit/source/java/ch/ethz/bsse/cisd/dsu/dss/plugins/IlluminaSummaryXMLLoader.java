/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.XMLInfraStructure;

/**
 * Loader of Illumina summary XML file.
 * 
 * @author Manuel Kohler
 */
class IlluminaSummaryXMLLoader
{

    private final Unmarshaller unmarshaller;

    private final XMLInfraStructure xmlInfraStructure;

    IlluminaSummaryXMLLoader(boolean validating)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(IlluminaSummary.class);
            unmarshaller = context.createUnmarshaller();
            xmlInfraStructure = new XMLInfraStructure(validating);
            xmlInfraStructure.setEntityResolver(new EntityResolver()
                {
                    public InputSource resolveEntity(String publicId, String systemId)
                            throws SAXException, IOException
                    {
                        String schemaVersion = systemId.substring(systemId.lastIndexOf('/'));
                        String resource =
                                "/"
                                        + IlluminaSummaryXMLLoader.class.getPackage().getName()
                                                .replace('.', '/') + schemaVersion;
                        InputStream inputStream =
                                IlluminaSummaryXMLLoader.class.getResourceAsStream(resource);
                        return inputStream == null ? null : new InputSource(inputStream);
                    }
                });

        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    IlluminaSummary readSummaryXML(File dataSet)
    {
        try
        {
            UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
            xmlInfraStructure.parse(new FileReader(dataSet), unmarshallerHandler);
            Object object = unmarshallerHandler.getResult();
            if (object instanceof IlluminaSummary == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            return (IlluminaSummary) object;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
