/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch;

import java.util.Map;

/**
 * @author pkupczyk
 */
public class MapBatch<K, V> extends Batch<Map<K, V>>
{

    public MapBatch(int batchIndex, int fromObjectIndex, int toObjectIndex, Map<K, V> objects, int totalObjectCount)
    {
        super(batchIndex, fromObjectIndex, toObjectIndex, objects, totalObjectCount);
    }

    public boolean isEmpty()
    {
        return getObjects() == null || getObjects().isEmpty();
    }

}
