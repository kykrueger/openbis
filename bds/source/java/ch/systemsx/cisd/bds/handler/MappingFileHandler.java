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

package ch.systemsx.cisd.bds.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.IDataStructureHandler;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.ReferenceType;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * A <code>IDataStructureHandler</code> implementation for the <code>standard_original_mapping</code> file.
 * 
 * @author Christian Ribeaud
 */
public final class MappingFileHandler implements IDataStructureHandler
{
    private final Map<String, Reference> standardOriginalMapping = new LinkedHashMap<String, Reference>();

    /** The directory the mapping is going to be written to. */
    private final IDirectory mappingDirectory;

    /** The root of {@link Reference#getPath()}. Usually the path to <code>standard</code> directory. */
    private final IDirectory pathRoot;

    /** The root of {@link Reference#getOriginalPath()}. Usually the path to <code>original</code> directory. */
    private final IDirectory originalPathRoot;

    /**
     * The mapping relating <i>standard</i> with <i>original</i> data.
     * <p>
     * Note that under format specific conditions this mapping file could be empty, meaning that the <i>standard</i>
     * directory contains the real data and not only links.
     * </p>
     */
    public static final String MAPPING_FILE = "standard_original_mapping";

    public MappingFileHandler(final IDirectory mappingDirectory, final IDirectory pathRoot,
            final IDirectory originalPathRoot)
    {
        assert mappingDirectory != null : "Given mapping directory can not be null.";
        assert pathRoot != null : "Given path root can not be null.";
        assert originalPathRoot != null : "Given original path root can not be null.";
        this.mappingDirectory = mappingDirectory;
        this.pathRoot = pathRoot;
        this.originalPathRoot = originalPathRoot;
    }

    /**
     * Returns the standard-original mapping.
     * 
     * @return an unmodifiable version of this map.
     */
    public final Map<String, Reference> getStandardOriginalMapping()
    {
        return Collections.unmodifiableMap(standardOriginalMapping);
    }

    /**
     * Adds a reference to the standard-original mapping.
     * 
     * @throws DataStructureException if a reference with the same path has already been registered.
     */
    public final void addReference(final Reference reference) throws DataStructureException
    {
        assert reference != null : "Unspecified reference.";
        final String path = reference.getPath();
        if (standardOriginalMapping.containsKey(path))
        {
            throw new DataStructureException("There is already a reference for file '" + path + "'.");
        }
        standardOriginalMapping.put(path, reference);
    }

    private final void loadStandardOriginalMapping()
    {
        final List<String> mappingLines = Utilities.getStringList(mappingDirectory, MAPPING_FILE);
        standardOriginalMapping.clear();
        for (int i = 0; i < mappingLines.size(); i++)
        {
            String referenceDefinition = mappingLines.get(i);
            int i1 = referenceDefinition.indexOf('\t');
            if (i1 < 0)
            {
                throw new DataStructureException("Error in standard-original mapping line " + (i + 1)
                        + ": missing first tab character: " + referenceDefinition);
            }
            String path = referenceDefinition.substring(0, i1);
            int i2 = referenceDefinition.indexOf('\t', i1 + 1);
            if (i2 < 0)
            {
                throw new DataStructureException("Error in standard-original mapping line " + (i + 1)
                        + ": missing second tab character: " + referenceDefinition);
            }
            final ReferenceType type = ReferenceType.resolveByShortName(referenceDefinition.substring(i1 + 1, i2));
            standardOriginalMapping.put(path, new Reference(path, referenceDefinition.substring(i2 + 1), type));
        }
    }

    private final String createMappingFile()
    {
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer, true);
        final Collection<Reference> values = standardOriginalMapping.values();
        for (final Reference reference : values)
        {
            final String path = reference.getPath();
            final String shortName = reference.getReferenceType().getShortName();
            final String originalPath = reference.getOriginalPath();
            printWriter.println(path + "\t" + shortName + "\t" + originalPath);
        }
        printWriter.close();
        return writer.toString();
    }

    //
    // IDataStructureHandler
    //

    public final void assertValid() throws DataStructureException
    {
        // TODO 2007-11-29, Christian Ribeaud: validation of loaded references. Note that this could interfere with
        // checksum validation. To validate a checksum, the file must exist. So we would not need to check the original
        // path existence here.
    }

    public final void performClosing()
    {
        mappingDirectory.addKeyValuePair(MAPPING_FILE, createMappingFile());
    }

    public final void performOpening()
    {
        loadStandardOriginalMapping();
    }

}
