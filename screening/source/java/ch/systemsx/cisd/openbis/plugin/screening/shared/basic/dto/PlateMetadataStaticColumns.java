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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

/**
 * Defines static columns of plate metadata row.
 * 
 * @author Izabela Adamczyk
 */
public enum PlateMetadataStaticColumns
{
    WELL("CODE"),

    TYPE("TYPE"),

    CONTENT("CONTENT"),

    CONTENT_TYPE("CONTENT_TYPE"),

    INHIBITED_GENE("INHIBITED_GENE"),

    GENE_DETAILS("GENE_DETAILS"),

    THUMBNAIL("THUMBNAIL");

    private final String id;

    private PlateMetadataStaticColumns(String id)
    {
        this.id = id;
    }

    public String colId()
    {
        return id;
    }
}
