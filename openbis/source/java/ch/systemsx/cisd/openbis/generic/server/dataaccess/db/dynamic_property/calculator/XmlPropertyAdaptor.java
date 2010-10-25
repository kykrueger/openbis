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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;

/**
 * {@link IEntityPropertyAdaptor} implementation for xml property.
 * 
 * @author Piotr Buczek
 */
class XmlPropertyAdaptor extends BasicPropertyAdaptor
{
    private final String xsltTransformation;

    public XmlPropertyAdaptor(String code, String value, EntityPropertyPE propertyPE,
            String xsltTransformation)
    {
        super(code, value, propertyPE);
        this.xsltTransformation = xsltTransformation;
    }

    public XmlPropertyAdaptor(String code, String value, String xsltTransformation)
    {
        this(code, value, null, xsltTransformation);
    }

    @Override
    public String renderedValue()
    {
        // TODO 2010-10-25, PTR: Refactoring needed (merge with XMLPropertyTransformer)
        final String xml = super.valueAsString();

        StringWriter writer = new StringWriter();
        try
        {
            Transformer transformer =
                    TransformerFactory.newInstance().newTransformer(
                            new StreamSource(new StringReader(xsltTransformation)));
            transformer
                    .transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
        } catch (TransformerException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return writer.toString();
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyTypeCode() + "\nvalue:\n" + valueAsString() + "\nrendered:\n"
                + renderedValue();
    }

}
