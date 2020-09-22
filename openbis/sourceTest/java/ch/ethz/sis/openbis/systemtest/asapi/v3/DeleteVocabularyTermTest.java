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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class DeleteVocabularyTermTest extends AbstractVocabularyTermTest
{

    @Test
    public void testDeleteTermUnauthorized()
    {
        final VocabularyTermPermId permId = new VocabularyTermPermId("HUMAN", "ORGANISM");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);

                    VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
                    options.setReason("Just for testing");

                    v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testDeleteTermNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(new VocabularyTermPermId("IDONTEXIST", "MENEITHER")), options);
    }

    @Test
    public void testDeleteTermUnused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "TEST_VOCABULARY";
        VocabularyTermPermId termIdA = new VocabularyTermPermId("TEST_TERM_A", vocabularyCode);
        VocabularyTermPermId termIdB = new VocabularyTermPermId("TEST_TERM_B", vocabularyCode);
        VocabularyTermPermId termIdC = new VocabularyTermPermId("TEST_TERM_C", vocabularyCode);

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdB);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        creation.setCode(termIdC.getCode());
        creation.setPreviousTermId(termIdA);

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdC, termIdB);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdC), options);

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdB);
    }

    @DataProvider
    public Object[][] providerTestDeleteTermFromInternallyManagedVocabulary()
    {
        return new Object[][] {
                { TEST_USER, true },
                { TEST_INSTANCE_ETLSERVER, true },
                { TEST_INSTANCE_OBSERVER, false },
                { TEST_SPACE_USER, false },
                { TEST_SPACE_ETLSERVER_TESTSPACE, false }
        };
    }

    @Test(dataProvider = "providerTestDeleteTermFromInternallyManagedVocabulary")
    public void testDeleteTermFromInternallyManagedVocabulary(String user, boolean allowed)
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        String userSessionToken = v3api.login(user, PASSWORD);

        VocabularyTermCreation term1Creation = new VocabularyTermCreation();
        term1Creation.setCode("I_WANT_TO_STAY");

        VocabularyTermCreation term2Creation = new VocabularyTermCreation();
        term2Creation.setCode("DELETE_ME");

        VocabularyCreation vocabularyCreation = new VocabularyCreation();
        vocabularyCreation.setCode("$INTERNALLY_MANAGED_" + UUID.randomUUID());
        vocabularyCreation.setManagedInternally(true);
        vocabularyCreation.setTerms(Arrays.asList(term1Creation, term2Creation));

        VocabularyTermPermId term1Id = new VocabularyTermPermId(term1Creation.getCode(), vocabularyCreation.getCode());
        VocabularyTermPermId term2Id = new VocabularyTermPermId(term2Creation.getCode(), vocabularyCreation.getCode());

        v3api.createVocabularies(adminSessionToken, Arrays.asList(vocabularyCreation));

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCreation.getCode());
        assertVocabularyTermPermIds(termsBefore, term1Id, term2Id);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        if (allowed)
        {
            v3api.deleteVocabularyTerms(userSessionToken, Arrays.asList(term2Id), options);

            List<VocabularyTerm> termsAfter = searchTerms(vocabularyCreation.getCode());
            assertVocabularyTermPermIds(termsAfter, term1Id);
        } else
        {
            try
            {
                v3api.deleteVocabularyTerms(userSessionToken, Arrays.asList(term2Id), options);
                fail();
            } catch (UserFailureException e)
            {
                assertTrue(e.getMessage().contains("Not allowed to delete terms from an internally managed vocabulary.")
                        || e.getMessage().contains("Access denied"), e.getMessage());
            }
        }
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Vocabulary 'GENDER'. is being used. Delete all connected data  first.*")
    public void testDeleteTermUsedInExperimentProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "GENDER";
        String experimentPermId = "200811050951882-1028";
        String experimentPropertyName = "GENDER";

        VocabularyTermPermId termIdMale = new VocabularyTermPermId("MALE", vocabularyCode);

        Experiment experiment = getExperiment(experimentPermId);
        assertEquals(experiment.getProperty(experimentPropertyName), termIdMale.getCode());

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdMale), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Vocabulary 'ORGANISM'. is being used. Delete all connected data  first.*")
    public void testDeleteTermUsedInSampleProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        String samplePermId = "200902091219327-1025";
        String samplePropertyName = "ORGANISM";

        VocabularyTermPermId termIdHuman = new VocabularyTermPermId("HUMAN", vocabularyCode);

        Sample sample = getSample(samplePermId);
        assertEquals(sample.getProperty(samplePropertyName), termIdHuman.getCode());

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdHuman), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Vocabulary 'GENDER'. is being used. Delete all connected data  first.*")
    public void testDeleteTermUsedInDataSetProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "GENDER";
        String dataSetPermId = "20081105092159111-1";
        String dataSetPropertyName = "GENDER";

        VocabularyTermPermId termIdFemale = new VocabularyTermPermId("FEMALE", vocabularyCode);

        DataSet dataSet = getDataSet(dataSetPermId);
        assertEquals(dataSet.getProperty(dataSetPropertyName), termIdFemale.getCode());

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdFemale), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Vocabulary 'ORGANISM'. is being used. Delete all connected data  first.*")
    public void testDeleteTermUsedInMaterialProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        String materialCode = "BACTERIUM-X";
        String materialTypeCode = "BACTERIUM";
        String materialPropertyName = "ORGANISM";

        VocabularyTermPermId termIdFly = new VocabularyTermPermId("FLY", vocabularyCode);

        Material material = getMaterial(materialCode, materialTypeCode);
        assertEquals(material.getProperty(materialPropertyName), termIdFly.getCode());

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdFly), options);
    }

    @Test
    public void testReplaceTermUnauthorized()
    {
        final VocabularyTermPermId termIdReplaced = new VocabularyTermPermId("HUMAN", "ORGANISM");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);

                    VocabularyTermPermId termIdReplacement = new VocabularyTermPermId("FLY", "ORGANISM");

                    VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
                    options.setReason("Just for testing");
                    options.replace(termIdReplaced, termIdReplacement);

                    v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdReplaced), options);
                }

            }, termIdReplaced);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*The following terms where not chosen to be deleted but had replacements specified: \\[VocabularyTermPE\\{code=HUMAN,label=<null>\\}\\].*")
    public void testReplaceTermNotDeleted()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        VocabularyTermPermId termIdDog = new VocabularyTermPermId("DOG", vocabularyCode);
        VocabularyTermPermId termIdHuman = new VocabularyTermPermId("HUMAN", vocabularyCode);
        VocabularyTermPermId termIdGorilla = new VocabularyTermPermId("GORILLA", vocabularyCode);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdHuman, termIdDog);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdGorilla), options);
    }

    @Test
    public void testReplaceTermNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        IVocabularyTermId termIdExisting = new VocabularyTermPermId("HUMAN", vocabularyCode);
        IVocabularyTermId termIdNonexistent = new VocabularyTermPermId("IDONTEXIST", vocabularyCode);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdNonexistent, termIdExisting);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdNonexistent), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Replacement term IDONTEXIST \\(ORGANISM\\) does not exist.*")
    public void testReplaceTermWithNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        IVocabularyTermId termIdExisting = new VocabularyTermPermId("HUMAN", vocabularyCode);
        IVocabularyTermId termIdNonexistent = new VocabularyTermPermId("IDONTEXIST", vocabularyCode);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdExisting, termIdNonexistent);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdExisting), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Replaced HUMAN \\(ORGANISM\\) and replacement TEST_TERM_A \\(TEST_VOCABULARY\\) terms cannot belong to different vocabularies.*")
    public void testReplaceTermWithFromDifferentVocabulary()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IVocabularyTermId termIdInVocabulary1 = new VocabularyTermPermId("HUMAN", "ORGANISM");
        IVocabularyTermId termIdInVocabulary2 = new VocabularyTermPermId("TEST_TERM_A", "TEST_VOCABULARY");

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdInVocabulary1, termIdInVocabulary2);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdInVocabulary1), options);
    }

    @Test
    public void testReplaceTermUnused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "TEST_VOCABULARY";
        VocabularyTermPermId termIdA = new VocabularyTermPermId("TEST_TERM_A", vocabularyCode);
        VocabularyTermPermId termIdB = new VocabularyTermPermId("TEST_TERM_B", vocabularyCode);
        VocabularyTermPermId termIdC = new VocabularyTermPermId("TEST_TERM_C", vocabularyCode);

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdB);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        creation.setCode(termIdC.getCode());
        creation.setPreviousTermId(termIdA);

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdC, termIdB);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdC, termIdA);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdC), options);

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdA, termIdB);
    }

    @Test
    public void testReplaceTermUsedInExperimentProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "GENDER";
        String experimentPermId = "200811050951882-1028";
        String experimentPropertyName = "GENDER";

        VocabularyTermPermId termIdMale = new VocabularyTermPermId("MALE", vocabularyCode);
        VocabularyTermPermId termIdFemale = new VocabularyTermPermId("FEMALE", vocabularyCode);

        Experiment experiment = getExperiment(experimentPermId);
        assertEquals(experiment.getProperty(experimentPropertyName), termIdMale.getCode());

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdMale, termIdFemale);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdMale, termIdFemale);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdMale), options);

        experiment = getExperiment(experimentPermId);
        assertEquals(experiment.getProperty(experimentPropertyName), termIdFemale.getCode());

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdFemale);

        assertExperimentsExists(experimentPermId, "200811050952663-1029", "200811050952663-1030");
    }

    @Test
    public void testReplaceTermUsedInSampleProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        String samplePermId = "200902091219327-1025";
        String samplePropertyName = "ORGANISM";

        VocabularyTermPermId termIdRat = new VocabularyTermPermId("RAT", vocabularyCode);
        VocabularyTermPermId termIdDog = new VocabularyTermPermId("DOG", vocabularyCode);
        VocabularyTermPermId termIdHuman = new VocabularyTermPermId("HUMAN", vocabularyCode);
        VocabularyTermPermId termIdGorilla = new VocabularyTermPermId("GORILLA", vocabularyCode);
        VocabularyTermPermId termIdFly = new VocabularyTermPermId("FLY", vocabularyCode);

        Sample sample = getSample(samplePermId);
        assertEquals(sample.getProperty(samplePropertyName), termIdHuman.getCode());

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdRat, termIdDog, termIdHuman, termIdGorilla, termIdFly);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdHuman, termIdRat);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdHuman), options);

        sample = getSample(samplePermId);
        assertEquals(sample.getProperty(samplePropertyName), termIdRat.getCode());

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdRat, termIdDog, termIdGorilla, termIdFly);

        assertSamplesExists(samplePermId);
    }

    @Test
    public void testReplaceTermUsedInDataSetProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "GENDER";
        String dataSetPermId = "20081105092159111-1";
        String dataSetPropertyName = "GENDER";

        VocabularyTermPermId termIdMale = new VocabularyTermPermId("MALE", vocabularyCode);
        VocabularyTermPermId termIdFemale = new VocabularyTermPermId("FEMALE", vocabularyCode);

        DataSet dataSet = getDataSet(dataSetPermId);
        assertEquals(dataSet.getProperty(dataSetPropertyName), termIdFemale.getCode());

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdMale, termIdFemale);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdFemale, termIdMale);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdFemale), options);

        dataSet = getDataSet(dataSetPermId);
        assertEquals(dataSet.getProperty(dataSetPropertyName), termIdMale.getCode());

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdMale);

        assertDataSetsExists(dataSetPermId);
    }

    @Test
    public void testReplaceTermUsedInMaterialProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String vocabularyCode = "ORGANISM";
        String materialCode = "BACTERIUM-X";
        String materialTypeCode = "BACTERIUM";
        String materialPropertyName = "ORGANISM";

        VocabularyTermPermId termIdRat = new VocabularyTermPermId("RAT", vocabularyCode);
        VocabularyTermPermId termIdDog = new VocabularyTermPermId("DOG", vocabularyCode);
        VocabularyTermPermId termIdHuman = new VocabularyTermPermId("HUMAN", vocabularyCode);
        VocabularyTermPermId termIdGorilla = new VocabularyTermPermId("GORILLA", vocabularyCode);
        VocabularyTermPermId termIdFly = new VocabularyTermPermId("FLY", vocabularyCode);

        Material material = getMaterial(materialCode, materialTypeCode);
        assertEquals(material.getProperty(materialPropertyName), termIdFly.getCode());

        List<VocabularyTerm> terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdRat, termIdDog, termIdHuman, termIdGorilla, termIdFly);

        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termIdFly, termIdDog);

        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termIdFly), options);

        material = getMaterial(materialCode, materialTypeCode);
        assertEquals(material.getProperty(materialPropertyName), termIdDog.getCode());

        terms = searchTerms(vocabularyCode);
        assertVocabularyTermPermIds(terms, termIdRat, termIdDog, termIdHuman, termIdGorilla);

        assertMaterialsExists(new MaterialPermId(materialCode, materialTypeCode));
    }

    @Test
    public void testReplaceTermWhichAlsoAccursInAnotherVocabulary()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertSampleProperty(sessionToken, "200811050919915-8", "$PLATE_GEOMETRY", "384_WELLS_16X24");
        assertSampleProperty(sessionToken, "200902091250077-1026", "ORGANISM", "GORILLA");
        assertTerms(sessionToken, "[VocabularyTerm DOG, "
                + "VocabularyTerm FLY, VocabularyTerm GORILLA, VocabularyTerm HUMAN, VocabularyTerm RAT]");

        // Create new vocabulary term 384_WELLS_16X24 in ORGANISM which also appears in $PLATE_GEOMETRY
        createTerm(sessionToken, "384_WELLS_16X24");
        assertTerms(sessionToken, "[VocabularyTerm 384_WELLS_16X24, VocabularyTerm DOG, "
                + "VocabularyTerm FLY, VocabularyTerm GORILLA, VocabularyTerm HUMAN, VocabularyTerm RAT]");

        // Delete term GORILLA and replace it by the new term 384_WELLS_16X24
        deleteAndReplaceTerm(sessionToken, "GORILLA", "384_WELLS_16X24");
        assertSampleProperty(sessionToken, "200811050919915-8", "$PLATE_GEOMETRY", "384_WELLS_16X24");
        assertSampleProperty(sessionToken, "200902091250077-1026", "ORGANISM", "384_WELLS_16X24");
        assertTerms(sessionToken, "[VocabularyTerm 384_WELLS_16X24, VocabularyTerm DOG, "
                + "VocabularyTerm FLY, VocabularyTerm HUMAN, VocabularyTerm RAT]");

        // Create again term GORILLA.
        createTerm(sessionToken, "GORILLA");
        assertTerms(sessionToken, "[VocabularyTerm 384_WELLS_16X24, VocabularyTerm DOG, "
                + "VocabularyTerm FLY, VocabularyTerm GORILLA, VocabularyTerm HUMAN, VocabularyTerm RAT]");

        // Delete term 384_WELLS_16X24 in ORGANISM and replace it by GORILLA
        deleteAndReplaceTerm(sessionToken, "384_WELLS_16X24", "GORILLA");
        assertSampleProperty(sessionToken, "200811050919915-8", "$PLATE_GEOMETRY", "384_WELLS_16X24");
        assertSampleProperty(sessionToken, "200902091250077-1026", "ORGANISM", "GORILLA");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermDeletionOptions o = new VocabularyTermDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteVocabularyTerms(sessionToken,
                Arrays.asList(new VocabularyTermPermId("TEST-LOGGING-1", "TEST"), new VocabularyTermPermId("TEST-LOGGING-2", "TEST")), o);

        assertAccessLog(
                "delete-vocabulary-terms  VOCABULARY_TERM_IDS('[TEST-LOGGING-1 (TEST), TEST-LOGGING-2 (TEST)]') DELETION_OPTIONS('VocabularyTermDeletionOptions[reason=test-reason]')");
    }

    private void deleteAndReplaceTerm(String sessionToken, String toBeDeleted, String replacement)
    {
        VocabularyTermPermId termToBeDeleted = new VocabularyTermPermId(toBeDeleted, "ORGANISM");
        VocabularyTermPermId replacementTerm = new VocabularyTermPermId(replacement, "ORGANISM");
        VocabularyTermDeletionOptions options = new VocabularyTermDeletionOptions();
        options.setReason("Just for testing");
        options.replace(termToBeDeleted, replacementTerm);
        v3api.deleteVocabularyTerms(sessionToken, Arrays.asList(termToBeDeleted), options);
    }

    private void createTerm(String sessionToken, String code)
    {
        VocabularyTermCreation termCreation = new VocabularyTermCreation();
        termCreation.setCode(code);
        VocabularyPermId vocabularyId = new VocabularyPermId("ORGANISM");
        termCreation.setVocabularyId(vocabularyId);
        v3api.createVocabularyTerms(sessionToken, Arrays.asList(termCreation));
    }

    private void assertTerms(String sessionToken, String expectedTerms)
    {
        VocabularyTermSearchCriteria searchCriteria = new VocabularyTermSearchCriteria();
        searchCriteria.withVocabulary().withCode().thatEquals("ORGANISM");
        List<VocabularyTerm> terms = v3api.searchVocabularyTerms(sessionToken, searchCriteria, new VocabularyTermFetchOptions()).getObjects();
        assertEquals(terms.toString(), expectedTerms);
    }

    private void assertSampleProperty(String sessionToken, String samplePermId, String property, String expectedValue)
    {
        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        SamplePermId plate = new SamplePermId(samplePermId);
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(plate), sampleFetchOptions).get(plate);
        assertEquals(sample.getProperties().get(property), expectedValue);
    }

}
