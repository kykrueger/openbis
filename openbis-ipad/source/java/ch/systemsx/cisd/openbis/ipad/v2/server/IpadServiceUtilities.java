/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.ipad.v2.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generally useful utility methods for implementing an ipad service.
 * 
 * @author cramakri
 */
public class IpadServiceUtilities
{

    /** Utility function for converting a list into a json-encoded list. */
    public static String jsonEncodedValue(Object thing)
    {
        try
        {
            return new ObjectMapper().writeValueAsString(thing);
        } catch (JsonGenerationException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        } catch (JsonMappingException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        } catch (IOException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        }
        return null;
    }

    /** Utility function to return an json-encoded empty list. */
    public static String jsonEmptyList()
    {
        return jsonEncodedValue(Collections.emptyList());
    }

    /** Utility function to return an json-encoded empty dictionary. */
    public static String jsonEmptyDict()
    {
        try
        {
            return new ObjectMapper().writeValueAsString(new HashMap<String, String>());
        } catch (JsonGenerationException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        } catch (JsonMappingException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        } catch (IOException e)
        {
            CheckedExceptionTunnel.wrapIfNecessary(e);
        }
        return null;
    }

}
