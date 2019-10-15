/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth;

import java.util.Set;

public interface ISQLAuthorisationInformationProviderDAO
{

    AuthorisationInformation findAuthorisedSpaceProjectIDs(Long userId);

    /**
     * Filters sample IDs based on their relations to space and projects.
     *
     * @param requestedIDs the IDs to be filtered
     * @param authInfo value object that contains space and project IDs, which should be related to the
     *     resulting IDs.
     * @return the subset of IDs which are related either to one of the specified projects or spaces.
     */
    Set<Long> getAuthorisedSamples(Set<Long> requestedIDs, AuthorisationInformation authInfo);

    /**
     * Filters experiment IDs based on their relations to space and projects.
     *
     * @param requestedIDs the IDs to be filtered
     * @param authInfo value object that contains space and project IDs, which should be related to the
     *     resulting IDs.
     * @return the subset of IDs which are related either to one of the specified projects or spaces.
     */
    Set<Long> getAuthorisedExperiments(Set<Long> requestedIDs, AuthorisationInformation authInfo);

}
