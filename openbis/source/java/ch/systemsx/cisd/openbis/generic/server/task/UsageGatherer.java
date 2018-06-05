/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

/**
 * @author Franz-Josef Elmer
 */
public class UsageGatherer
{
    static final String UNNAMED_GROUP = "$all$";

    private IApplicationServerInternalApi service;

    public UsageGatherer(IApplicationServerInternalApi service)
    {
        this.service = service;
    }

    public Map<String, Map<String, UsageInfo>> gatherUsage(Period period, List<String> groupsOrNull)
    {
        String sessionToken = service.loginAsSystem();
        try
        {
            Map<String, Map<String, UsageInfo>> usageByUsersAndGroups = createUsageByUsersAndGroupsMap(sessionToken, groupsOrNull);
            gatherNewExperimentUsage(sessionToken, usageByUsersAndGroups, period);
            gatherNewSampleUsage(sessionToken, usageByUsersAndGroups, period);
            gatherNewDataSetUsage(sessionToken, usageByUsersAndGroups, period);
            return usageByUsersAndGroups;
        } finally
        {
            service.logout(sessionToken);
        }
    }

    private Map<String, Map<String, UsageInfo>> createUsageByUsersAndGroupsMap(String sessionToken, List<String> groupsOrNull)
    {
        Map<String, Map<String, UsageInfo>> result = new TreeMap<>();
        for (String user : getAllUsers(sessionToken))
        {
            Map<String, UsageInfo> usageByGroups = new TreeMap<>();
            usageByGroups.put(groupsOrNull == null ? UNNAMED_GROUP : user, new UsageInfo());
            result.put(user, usageByGroups);
        }
        if (groupsOrNull != null)
        {
            AuthorizationGroupFetchOptions groupFetchOptions = new AuthorizationGroupFetchOptions();
            groupFetchOptions.withUsers();
            List<AuthorizationGroupPermId> groupIds = groupsOrNull.stream().map(AuthorizationGroupPermId::new).collect(Collectors.toList());
            Collection<AuthorizationGroup> groups = service.getAuthorizationGroups(sessionToken, groupIds, groupFetchOptions).values();
            for (AuthorizationGroup group : groups)
            {
                List<Person> users = group.getUsers();
                for (Person user : users)
                {
                    result.get(user.getUserId()).put(group.getCode(), new UsageInfo());
                }
            }
        }
        return result;
    }

    private Set<String> getAllUsers(String sessionToken)
    {
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        List<Person> users = service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
        return users.stream().map(Person::getUserId).collect(Collectors.toSet());
    }

    private void gatherNewExperimentUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndGroups, Period period)
    {
        ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
        setPeriod(searchCriteria, period);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withRegistrator();
        List<Experiment> experiments = service.searchExperiments(sessionToken, searchCriteria, fetchOptions).getObjects();
        Function<Experiment, String> spaceExtractor = exp -> exp.getProject().getSpace().getCode();
        gatherUsage(usageByUsersAndGroups, experiments, spaceExtractor, UsageInfo::addNewExperiment);
    }

    private void gatherNewSampleUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndGroups, Period period)
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        setPeriod(searchCriteria, period);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        List<Sample> samples = service.searchSamples(sessionToken, searchCriteria, fetchOptions).getObjects();
        Function<Sample, String> spaceExtractor = sample -> sample.getSpace().getCode();
        gatherUsage(usageByUsersAndGroups, samples, spaceExtractor, UsageInfo::addNewSample);
    }

    private void gatherNewDataSetUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndGroups, Period period)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        setPeriod(searchCriteria, period);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withRegistrator();
        SampleFetchOptions sampleFetchOptions = fetchOptions.withSample();
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withRegistrator();
        ExperimentFetchOptions experimentFetchOptions = fetchOptions.withExperiment();
        experimentFetchOptions.withProject().withSpace();
        experimentFetchOptions.withRegistrator();
        List<DataSet> dataSets = service.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        List<IRegistratorHolder> entities = new ArrayList<>();
        for (DataSet dataSet : dataSets)
        {
            Experiment experiment = dataSet.getExperiment();
            Sample sample = dataSet.getSample();
            if (experiment != null)
            {
                entities.add(experiment);
            } else if (sample != null)
            {
                entities.add(sample);
            }
        }
        Function<IRegistratorHolder, String> spaceExtractor = e -> e instanceof Sample ? ((Sample) e).getSpace().getCode()
                : ((Experiment) e).getProject().getSpace().getCode();
        gatherUsage(usageByUsersAndGroups, entities, spaceExtractor, UsageInfo::addNewDataSet);
    }

    private <T extends IRegistratorHolder> void gatherUsage(Map<String, Map<String, UsageInfo>> usageByUsersAndGroups, List<T> entities,
            Function<T, String> spaceExtractor, Consumer<UsageInfo> consumer)
    {
        for (T entity : entities)
        {
            String userId = entity.getRegistrator().getUserId();
            Map<String, UsageInfo> usageByGroups = usageByUsersAndGroups.get(userId);
            if (usageByGroups != null)
            {
                String space = spaceExtractor.apply(entity);
                UsageInfo usageInfo = getUsageInfo(usageByGroups, space, userId);
                consumer.accept(usageInfo);
            }
        }
    }

    private UsageInfo getUsageInfo(Map<String, UsageInfo> usageByGroups, String space, String userId)
    {
        String[] parts = space.split("_");
        UsageInfo usageInfo = parts.length > 1 ? usageByGroups.get(parts[0]) : null;
        if (usageInfo == null)
        {
            usageInfo = usageByGroups.get(UNNAMED_GROUP);
        }
        if (usageInfo == null)
        {
            usageInfo = usageByGroups.get(userId);
        }
        return usageInfo;
    }

    private void setPeriod(AbstractEntitySearchCriteria<?> searchCriteria, Period period)
    {
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(period.getFrom());
        searchCriteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(period.getUntil());
    }
}
