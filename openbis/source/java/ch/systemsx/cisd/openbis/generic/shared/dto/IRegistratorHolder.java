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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * Implementations are aware of the registrator.
 * 
 * @author Franz-Josef Elmer
 */
public interface IRegistratorHolder
{

    /**
     * Returns the registrator.
     * 
     * @return <code>null</code> if undefined.
     */
    public PersonPE getRegistrator();

    /**
     * Sets the person who has registered something.
     */
    public void setRegistrator(final PersonPE registrator);

}