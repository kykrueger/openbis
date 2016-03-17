/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateVocabularyTermExecutor implements ICreateVocabularyTermExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IMapVocabularyByIdExecutor mapVocabularyByIdExecutor;

    @Autowired
    private IMapVocabularyTermByIdExecutor mapVocabularyTermByIdExecutor;

    @Autowired
    private ICreateVocabularyTermAuthorizationExecutor authorizationExecutor;

    @Override
    public List<VocabularyTermPermId> create(IOperationContext context, List<VocabularyTermCreation> creations)
    {
        checkData(context, creations);
        checkAccess(context, creations);

        Map<IVocabularyId, Collection<VocabularyTermCreation>> termsMap = new HashMap<IVocabularyId, Collection<VocabularyTermCreation>>();
        Set<IVocabularyTermId> previousTermsIds = new HashSet<IVocabularyTermId>();

        for (VocabularyTermCreation creation : creations)
        {
            Collection<VocabularyTermCreation> terms = termsMap.get(creation.getVocabularyId());
            if (terms == null)
            {
                terms = new LinkedList<VocabularyTermCreation>();
                termsMap.put(creation.getVocabularyId(), terms);
            }
            terms.add(creation);

            if (creation.getPreviousTermId() != null)
            {
                previousTermsIds.add(creation.getPreviousTermId());
            }
        }

        Map<IVocabularyId, VocabularyPE> vocabularyMap = mapVocabularyByIdExecutor.map(context, termsMap.keySet());
        Map<VocabularyTermCreation, VocabularyTermPE> createdTermsMap = new HashMap<VocabularyTermCreation, VocabularyTermPE>();

        for (Map.Entry<IVocabularyId, VocabularyPE> vocabularyEntry : vocabularyMap.entrySet())
        {
            IVocabularyId vocabularyId = vocabularyEntry.getKey();
            VocabularyPE vocabulary = vocabularyEntry.getValue();

            if (vocabulary == null)
            {
                throw new UserFailureException("Vocabulary with id " + vocabularyId + " does not exist.");
            }

            createdTermsMap.putAll(createTerms(context, vocabulary, termsMap.get(vocabularyId)));
        }

        List<VocabularyTermPermId> permIds = new ArrayList<VocabularyTermPermId>(creations.size());

        for (VocabularyTermCreation creation : creations)
        {
            VocabularyTermPE createdTerm = createdTermsMap.get(creation);
            permIds.add(new VocabularyTermPermId(createdTerm.getVocabulary().getCode(), createdTerm.getCode()));
        }

        return permIds;
    }

    private void checkData(IOperationContext context, Collection<VocabularyTermCreation> creations)
    {
        for (VocabularyTermCreation creation : creations)
        {
            if (creation.getVocabularyId() == null)
            {
                throw new UserFailureException("Vocabulary term vocabulary id cannot be null");
            }
            if (creation.getCode() == null || creation.getCode().trim().length() == 0)
            {
                throw new UserFailureException("Vocabulary term code cannot be null or empty.");
            }
        }
    }

    private void checkAccess(IOperationContext context, Collection<VocabularyTermCreation> creations)
    {
        boolean hasOfficial = false;
        boolean hasUnofficial = false;

        for (VocabularyTermCreation creation : creations)
        {
            if (creation.isOfficial())
            {
                hasOfficial = true;
            } else
            {
                hasUnofficial = true;
            }
        }

        if (hasOfficial)
        {
            authorizationExecutor.checkCreateOfficialTerm(context);
        }
        if (hasUnofficial)
        {
            authorizationExecutor.checkCreateUnofficialTerm(context);
        }
    }

    private Map<VocabularyTermCreation, VocabularyTermPE> createTerms(IOperationContext context, VocabularyPE vocabulary,
            Collection<VocabularyTermCreation> creations)
    {
        Map<VocabularyTermCreation, VocabularyTermPE> results = new HashMap<VocabularyTermCreation, VocabularyTermPE>();

        IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(context.getSession());
        vocabularyBO.loadDataByTechId(new TechId(vocabulary.getId()));
        vocabularyBO.setAllowChangingInternallyManaged(isAllowedToChangeInternallyManaged(context));

        Set<String> existingTermCodes = new HashSet<String>();
        for (VocabularyTermPE existingTerm : vocabulary.getTerms())
        {
            existingTermCodes.add(existingTerm.getCode());
        }

        for (VocabularyTermCreation creation : creations)
        {
            if (existingTermCodes.contains(creation.getCode()))
            {
                throw new UserFailureException("Vocabulary term " + creation.getCode() + " (" + vocabulary.getCode() + ") already exists.");
            }

            Long previousTermOrdinal = getPreviousTermOrdinal(context, vocabulary, creation);

            if (creation.isOfficial())
            {
                VocabularyTerm term = new VocabularyTerm();
                term.setCode(creation.getCode());
                term.setLabel(creation.getLabel());
                term.setDescription(creation.getDescription());

                List<VocabularyTermPE> termPEs = vocabularyBO.addNewTerms(Arrays.asList(term), previousTermOrdinal);

                results.put(creation, termPEs.get(0));
            } else
            {
                VocabularyTermPE termPE = vocabularyBO.addNewUnofficialTerm(creation.getCode(), creation.getLabel(), creation.getDescription(),
                        previousTermOrdinal);

                results.put(creation, termPE);
            }
        }

        vocabularyBO.save();

        return results;
    }

    private boolean isAllowedToChangeInternallyManaged(IOperationContext context)
    {
        Set<RoleAssignmentPE> roles = context.getSession().tryGetCreatorPerson().getAllPersonRoles();

        for (RoleAssignmentPE role : roles)
        {
            if (RoleCode.ETL_SERVER.equals(role.getRole()) || (RoleCode.ADMIN.equals(role.getRole()) && role.getSpace() == null))
            {
                return true;
            }
        }

        return false;
    }

    private Long getPreviousTermOrdinal(IOperationContext context, VocabularyPE vocabulary, VocabularyTermCreation creation)
    {
        if (creation.getPreviousTermId() != null)
        {
            Map<IVocabularyTermId, VocabularyTermPE> previousTermMap =
                    mapVocabularyTermByIdExecutor.map(context, Arrays.asList(creation.getPreviousTermId()));
            VocabularyTermPE previousTerm = previousTermMap.get(creation.getPreviousTermId());

            if (previousTerm == null)
            {
                throw new UserFailureException("Position of term " + creation.getCode() + " (" + creation.getVocabularyId()
                        + ") could not be found as the specified previous term " + creation.getPreviousTermId() + " does not exist.");
            } else if (false == previousTerm.getVocabulary().getCode().equals(vocabulary.getCode()))
            {
                throw new UserFailureException("Position of term " + creation.getCode() + " (" + creation.getVocabularyId()
                        + ") could not be found as the specified previous term " + creation.getPreviousTermId() + " is in a different vocabulary ("
                        + previousTerm.getVocabulary().getCode() + ").");
            } else
            {
                // Need to refresh the term object as it might have been manually updated with
                // an SQL statement inside vocabularyBO (see increaseVocabularyTermOrdinals).
                // Do it only when the term already exists in the database, otherwise the refresh
                // method throws an exception.

                Session session = daoFactory.getSessionFactory().getCurrentSession();

                session.flush();
                session.refresh(previousTerm);

                return previousTerm.getOrdinal();
            }
        } else

        {
            return null;
        }
    }

}
