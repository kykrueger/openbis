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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

final class ProteinDescription
{
    static final String DESCRIPTION_KEY = "DE";
    static final String SEQUENCE_KEY = "SEQ";
    
    static String createKeyValuePair(String key, String value)
    {
        return "\\" + key + "=" + value;
    }
    
    private final String uniprotID;
    private final String description;
    private final String sequence;

    public ProteinDescription(String proteinDescription)
    {
        String[] items = proteinDescription.split("\\\\");
        uniprotID = tryToGetUniprotID(items);
        description = tryToGetValue(items, DESCRIPTION_KEY);
        sequence = tryToGetValue(items, SEQUENCE_KEY);
    }
    
    public final String getUniprotID()
    {
        return uniprotID;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getSequence()
    {
        return sequence;
    }

    private String tryToGetUniprotID(String[] items)
    {
        return items == null || items.length == 0 ? null : items[0].trim(); 
    }
    
    private String tryToGetValue(String[] items, String key)
    {
        for (String item : items)
        {
            int indexOfEqualSign = item.indexOf('=');
            if (indexOfEqualSign > 0
                    && item.substring(0, indexOfEqualSign).trim().equalsIgnoreCase(key))
            {
                return item.substring(indexOfEqualSign + 1).trim();
            }
        }
        return null;
    }
}