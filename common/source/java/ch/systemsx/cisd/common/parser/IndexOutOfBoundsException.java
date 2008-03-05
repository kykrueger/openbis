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

package ch.systemsx.cisd.common.parser;

/**
 * A <code>ParserException</code> extension which signalizes a lookup index outside of the currently available tokens.
 * 
 * @author Christian Ribeaud
 */
public final class IndexOutOfBoundsException extends ParserException
{

    static final String MESSAGE_FORMAT = "Not enough tokens are available (index: %d, available: %d)";

    private static final long serialVersionUID = 1L;

    private final int column;

    private final String[] lineTokens;

    public IndexOutOfBoundsException(final int index, final String[] lineTokens)
    {
        super(createMessage(index, lineTokens));
        this.column = index;
        this.lineTokens = lineTokens;
    }

    private final static String createMessage(final int index, final String[] lineTokens)
    {
        assert lineTokens != null : "Line tokens can not be null.";
        assert index >= lineTokens.length : "Index must be out of range (otherwise no reason to call this exception).";
        return String.format(MESSAGE_FORMAT, index, lineTokens.length);
    }

    public final int getColumn()
    {
        return column;
    }

    public final String[] getLineTokens()
    {
        return lineTokens;
    }

}
