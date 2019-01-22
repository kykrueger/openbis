/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.Date;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseRecord;

/**
 * @author pkupczyk
 */
public class SampleBaseRecord extends ObjectBaseRecord
{

    public String code;

    public String spaceCode;

    public String projectCode;

    public String containerCode;

    public String permId;

    public boolean frozen;

    public boolean frozenForComponents;

    public boolean frozenForChildren;

    public boolean frozenForParents;

    public boolean frozenForDataSets;

    public Date registrationDate;

    public Date modificationDate;

}
