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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Inmutable value object of a versioned format.
 *
 * @author Franz-Josef Elmer
 */
public class Format
{
    static final String FORMAT_CODE_FILE = "format_code";
    static final String FORMAT_DIR = "format";
    static final String FORMAT_VARIANT_FILE = "format_variant";

    /**
     * Loads the format from the specified directory.
     * 
     * @throws DataStructureException if the format could be loaded.
     */
    static Format loadFrom(IDirectory directory)
    {
        INode dir = directory.tryToGetNode(FORMAT_DIR);
        if (dir instanceof IDirectory == false)
        {
            throw new DataStructureException("Not a directory: " + dir);
        }
        IDirectory formatDir = (IDirectory) dir;
        INode file = formatDir.tryToGetNode(FORMAT_CODE_FILE);
        if (file instanceof IFile == false)
        {
            throw new DataStructureException("Not a plain file: " + file);
        }
        IFile codeFile = (IFile) file;
        String formatCode = codeFile.getStringContent().trim();
        Version formatVersion = Version.loadFrom(formatDir);
        String variant = null;
        file = formatDir.tryToGetNode(FORMAT_VARIANT_FILE);
        if (file != null)
        {
            if (file instanceof IFile == false)
            {
                throw new DataStructureException("Not a plain file: " + file);
            }
            variant = ((IFile) file).getStringContent().trim();
        }
        return new Format(formatCode, formatVersion, variant);
    }
    
    private final String code;
    private final Version version;
    private final String variant;

    /**
     * Creates a new instance based on the specified format code, format variant (optional), and version.
     */
    public Format(String code, Version version, String variantOrNull)
    {
        assert code != null : "Unspecified format code.";
        assert version != null : "Unpsecified version.";
        this.code = code;
        this.version = version;
        variant = variantOrNull;
    }
    
    /**
     * Returns the format code.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Returns the format version.
     */
    public final Version getVersion()
    {
        return version;
    }

    /**
     * Returns the format variant.
     * 
     * @return <code>null</code> if undefined.
     */
    public final String getVariant()
    {
        return variant;
    }

    void saveTo(IDirectory directory)
    {
        IDirectory dir = directory.makeDirectory(FORMAT_DIR);
        dir.addKeyValuePair(FORMAT_CODE_FILE, code);
        version.saveTo(dir);
        if (variant != null)
        {
            dir.addKeyValuePair(FORMAT_VARIANT_FILE, variant);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Format == false)
        {
            return false;
        }
        Format format = (Format) obj;
        return format.code.equals(code) && format.version.equals(version) 
                && (format.variant == null ? null == variant : format.variant.equals(variant));
    }

    @Override
    public int hashCode()
    {
        return (code.hashCode() * 37 + version.hashCode()) * 37 + (variant == null ? 0 : variant.hashCode());
    }

    @Override
    public String toString()
    {
        return "Format: " + code + " " + version + (variant == null ? "" : "[" + variant + "]");
    }

}
