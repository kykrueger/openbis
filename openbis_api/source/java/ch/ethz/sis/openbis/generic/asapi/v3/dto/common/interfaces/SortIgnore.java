/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * An annotation that can be used to mark methods that should not be considered as candidates for V3 sorting, e.g. because they operate on data that
 * cannot be sorted or are just helper methods.
 * 
 * @author pkupczyk
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@JsonObject("as.dto.common.interfaces.SortIgnore")
public @interface SortIgnore
{

}
