/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.DATE_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.DATE_HOURS_MINUTES_SECONDS_FORMAT;
import static org.testng.Assert.assertEquals;

import java.text.DateFormat;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchMaterialTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new MaterialSearchCriteria(), 3734);
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withId().thatEquals(permId);
        testSearch(TEST_USER, criteria, permId);
    }

    @Test
    public void testSearchWithIdSetToPermIdSortByPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withId().thatEquals(permId);
        MaterialFetchOptions options = new MaterialFetchOptions();
        options.sortBy().permId();
        testSearch(TEST_USER, criteria, options, permId);
    }

    @Test
    public void testSearchWithPermId()
    {
        final MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withPermId().thatEquals("NOT SUPPORTED YET");
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testSearch(TEST_USER, criteria);
                }
            }, "Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
    }

    @Test
    public void testSearchWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withCode().thatStartsWith("VIRUS");
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithCodes()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("VIRUS2", "VIRUS1"));
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("BACTERIUM"));
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withPermId().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenovirus 5");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenovirus");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenovirus 3");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denovirus 5");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("denoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION");
        testSearch(TEST_USER, criteria, 40);
    }

    @Test
    public void testSearchWithDateProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2007-07-17");
        testSearch(TEST_USER, criteria, new MaterialPermId("NEUTRAL", "CONTROL"), new MaterialPermId("C-NO-SEC", "CONTROL"), new MaterialPermId(
                "INHIBITOR", "CONTROL"));
    }

    @Test
    public void testSearchForMaterialWithIntegerPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("INTEGER_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("INT_NUMBER", "123");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaStartsWithMatch = new MaterialSearchCriteria();
        criteriaStartsWithMatch.withProperty("INT_NUMBER").thatStartsWith("12");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaStartsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "INTEGER"));

        final MaterialSearchCriteria criteriaEndsWithMatch = new MaterialSearchCriteria();
        criteriaEndsWithMatch.withProperty("INT_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaEndsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "INTEGER"));

        final MaterialSearchCriteria criteriaContainsMatch = new MaterialSearchCriteria();
        criteriaContainsMatch.withProperty("INT_NUMBER").thatContains("23");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaContainsMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "INTEGER"));
    }

    @Test
    public void testSearchForMaterialWithBooleanProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("BOOLEAN_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("BOOLEAN", "false");

        final MaterialPermId createdMaterialPermId = v3api.createMaterials(sessionToken,
                Collections.singletonList(materialCreation)).get(0);

        final MaterialSearchCriteria falseValueCriterion = new MaterialSearchCriteria();
        falseValueCriterion.withProperty("BOOLEAN").thatEquals("false");
        testSearch(TEST_USER, falseValueCriterion, createdMaterialPermId);
        
        final MaterialSearchCriteria trueValueCriterion = new MaterialSearchCriteria();
        trueValueCriterion.withProperty("BOOLEAN").thatEquals("true");
        testSearch(TEST_USER, trueValueCriterion);
    }

    @Test
    public void testSearchForMaterialWithBooleanPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("BOOLEAN_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("BOOLEAN", "false");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaStartsWithMatch = new MaterialSearchCriteria();
        criteriaStartsWithMatch.withProperty("BOOLEAN").thatStartsWith("fa");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaStartsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "BOOLEAN"));

        final MaterialSearchCriteria criteriaEndsWithMatch = new MaterialSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEndsWith("lse");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaEndsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "BOOLEAN"));

        final MaterialSearchCriteria criteriaContainsMatch = new MaterialSearchCriteria();
        criteriaContainsMatch.withProperty("BOOLEAN").thatContains("als");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaContainsMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "BOOLEAN"));

        final MaterialSearchCriteria criteriaLTMatch = new MaterialSearchCriteria();
        criteriaLTMatch.withProperty("BOOLEAN").thatIsLessThan("true");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaLTMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThan", "BOOLEAN"));

        final MaterialSearchCriteria criteriaLEMatch = new MaterialSearchCriteria();
        criteriaLEMatch.withProperty("BOOLEAN").thatIsLessThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaLEMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThanOrEqualTo", "BOOLEAN"));

        final MaterialSearchCriteria criteriaGTMatch = new MaterialSearchCriteria();
        criteriaGTMatch.withProperty("BOOLEAN").thatIsGreaterThan("true");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaGTMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThan", "BOOLEAN"));

        final MaterialSearchCriteria criteriaGEMatch = new MaterialSearchCriteria();
        criteriaGEMatch.withProperty("BOOLEAN").thatIsGreaterThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaGEMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThanOrEqualTo", "BOOLEAN"));
    }

    @Test
    public void testSearchForMaterialWithRealPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("REAL_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("REAL_NUMBER", "1.23");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaStartsWithMatch = new MaterialSearchCriteria();
        criteriaStartsWithMatch.withProperty("REAL_NUMBER").thatStartsWith("1.2");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaStartsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "REAL"));

        final MaterialSearchCriteria criteriaEndsWithMatch = new MaterialSearchCriteria();
        criteriaEndsWithMatch.withProperty("REAL_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaEndsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "REAL"));

        final MaterialSearchCriteria criteriaContainsMatch = new MaterialSearchCriteria();
        criteriaContainsMatch.withProperty("REAL_NUMBER").thatContains(".2");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaContainsMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "REAL"));
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUHU");
        testSearch(TEST_USER, criteria, new MaterialPermId("MYGENE1", "GENE"));

        criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUH");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyField()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyField().thatEquals("/VIRUS1");
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithModificationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModificationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByPropertyWithFloatValues()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("GFP", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("SCRAM", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("XXXXX-ALL", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-DESC", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-SIZE", "CONTROL"));

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withProperties();

        fo.sortBy().property("VOLUME").asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials1.get(0).getProperty("VOLUME"), "2.2");
        assertEquals(materials1.get(1).getProperty("VOLUME"), "3.0");
        assertEquals(materials1.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials1.get(3).getProperty("VOLUME"), "99.99");
        assertEquals(materials1.get(4).getProperty("VOLUME"), "123");

        fo.sortBy().property("VOLUME").desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials2.get(0).getProperty("VOLUME"), "123");
        assertEquals(materials2.get(1).getProperty("VOLUME"), "99.99");
        assertEquals(materials2.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials2.get(3).getProperty("VOLUME"), "3.0");
        assertEquals(materials2.get(4).getProperty("VOLUME"), "2.2");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("SRM");
        criteria.withCode().thatContains("1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithOrOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("SRM_1");
        criteria.withCode().thatEquals("SRM_1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();

        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("MYGENE2", "GENE"));

        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("MYGENE2", "GENE"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("MYGENE1", "GENE"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("MYGENE1", "GENE"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withCode().thatEquals("VIRUS");

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withRegistrator();
        fo.withProperties();

        v3api.searchMaterials(sessionToken, c, fo);

        assertAccessLog(
                "search-materials  SEARCH_CRITERIA:\n'MATERIAL\n    with attribute 'code' equal to 'VIRUS'\n'\nFETCH_OPTIONS:\n'Material\n    with Registrator\n    with Properties\n'");
    }

    @Test
    public void testSearchNumeric()
    {
        // VOLUME: 99.99 CODE: GFP
        // VOLUME: 123 CODE: SCRAM
        // VOLUME: 3.0 CODE: X-NO-DESC
        // VOLUME: 22.22 CODE: X-NO-SIZE
        // VOLUME: 2.2 CODE: XXXXX-ALL
        
        // OFFSET: 123 CODE: 913_A
        // OFFSET: 321 CODE: 913_B
        // OFFSET: 111111 CODE: 913_C
        // OFFSET: 4711 CODE: OLI_1
        // OFFSET: 3 CODE: XX333_B
        // OFFSET: 123 CODE: XX444_A

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialFetchOptions sortByCodeFO = new MaterialFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals
        final MaterialSearchCriteria criteriaGOE = new MaterialSearchCriteria();
        criteriaGOE.withNumberProperty("VOLUME").thatIsGreaterThanOrEqualTo(99.99);
        final List<Material> materialsGOE = searchMaterials(sessionToken, criteriaGOE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOE, "GFP (CONTROL)", "SCRAM (CONTROL)");

        // Greater or Equals - Providing integer as real
        final MaterialSearchCriteria criteriaGOEIR = new MaterialSearchCriteria();
        criteriaGOEIR.withNumberProperty("OFFSET").thatIsGreaterThanOrEqualTo(321.0);
        final List<Material> materialsGOEIR = searchMaterials(sessionToken, criteriaGOEIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOEIR, "913_B (SIRNA)", "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater or Equals - Providing integer
        final MaterialSearchCriteria criteriaGOEI = new MaterialSearchCriteria();
        criteriaGOEI.withNumberProperty("OFFSET").thatIsGreaterThanOrEqualTo(321);
        final List<Material> materialsGOEI = searchMaterials(sessionToken, criteriaGOEI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOEI, "913_B (SIRNA)", "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater
        final MaterialSearchCriteria criteriaG = new MaterialSearchCriteria();
        criteriaG.withNumberProperty("VOLUME").thatIsGreaterThan(99.99);
        final List<Material> materialsG = searchMaterials(sessionToken, criteriaG, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsG, "SCRAM (CONTROL)");

        // Greater - Providing integer as real
        final MaterialSearchCriteria criteriaGIR = new MaterialSearchCriteria();
        criteriaGIR.withNumberProperty("OFFSET").thatIsGreaterThan(321.0);
        final List<Material> materialsGIR = searchMaterials(sessionToken, criteriaGIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGIR, "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater - Providing integer
        final MaterialSearchCriteria criteriaGI = new MaterialSearchCriteria();
        criteriaGI.withNumberProperty("OFFSET").thatIsGreaterThan(321);
        final List<Material> materialsGI = searchMaterials(sessionToken, criteriaGI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGI, "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Equals As Text - Real
        final MaterialSearchCriteria criteriaETxt2 = new MaterialSearchCriteria();
        criteriaETxt2.withProperty("OFFSET").thatEquals("123.0");
        final List<Material> materialsETxt2 = searchMaterials(sessionToken, criteriaETxt2, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsETxt2, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Equals As Text - Integer
        MaterialSearchCriteria criteriaETxt = new MaterialSearchCriteria();
        criteriaETxt.withProperty("OFFSET").thatEquals("123");
        List<Material> materialsETxt = searchMaterials(sessionToken, criteriaETxt, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsETxt, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Equals
        MaterialSearchCriteria criteriaE = new MaterialSearchCriteria();
        criteriaE.withNumberProperty("OFFSET").thatEquals(123);
        List<Material> materialsE = searchMaterials(sessionToken, criteriaE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsE, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Less or Equals
        final MaterialSearchCriteria criteriaLOE = new MaterialSearchCriteria();
        criteriaLOE.withNumberProperty("VOLUME").thatIsLessThanOrEqualTo(99.99);
        final List<Material> materialsLOE = searchMaterials(sessionToken, criteriaLOE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOE, "GFP (CONTROL)", "X-NO-DESC (CONTROL)",  "X-NO-SIZE (CONTROL)",
                "XXXXX-ALL (CONTROL)");

        // Less or Equals - Providing integer as real
        final MaterialSearchCriteria criteriaLOEIR = new MaterialSearchCriteria().withAndOperator();
        criteriaLOEIR.withNumberProperty("OFFSET").thatIsLessThanOrEqualTo(321.0);
        criteriaLOEIR.withNumberProperty("OFFSET").thatIsGreaterThan(1.0);
        final List<Material> materialsLOEIR = searchMaterials(sessionToken, criteriaLOEIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOEIR, "913_A (SIRNA)", "913_B (SIRNA)", "XX333_B (SIRNA)",
                "XX444_A (SIRNA)");

        // Less or Equals - Providing integer
        final MaterialSearchCriteria criteriaLOEI = new MaterialSearchCriteria().withAndOperator();
        criteriaLOEI.withNumberProperty("OFFSET").thatIsLessThanOrEqualTo(321);
        criteriaLOEI.withNumberProperty("OFFSET").thatIsGreaterThan(1);
        final List<Material> materialsLOEI = searchMaterials(sessionToken, criteriaLOEI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOEI, "913_A (SIRNA)", "913_B (SIRNA)", "XX333_B (SIRNA)",
                "XX444_A (SIRNA)");

        // Less
        final MaterialSearchCriteria criteriaL = new MaterialSearchCriteria();
        criteriaL.withNumberProperty("VOLUME").thatIsLessThan(99.99);
        final List<Material> materialsL = searchMaterials(sessionToken, criteriaL, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsL, "X-NO-DESC (CONTROL)",  "X-NO-SIZE (CONTROL)",
                "XXXXX-ALL (CONTROL)");

        // Less - Providing integer as real
        final MaterialSearchCriteria criteriaLIR = new MaterialSearchCriteria().withAndOperator();
        criteriaLIR.withNumberProperty("OFFSET").thatIsLessThan(321.0);
        criteriaLIR.withNumberProperty("OFFSET").thatIsGreaterThan(1.0);
        final List<Material> materialsLIR = searchMaterials(sessionToken, criteriaLIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLIR, "913_A (SIRNA)", "XX333_B (SIRNA)", "XX444_A (SIRNA)");

        // Greater - Providing integer
        final MaterialSearchCriteria criteriaLI = new MaterialSearchCriteria();
        criteriaLI.withNumberProperty("OFFSET").thatIsLessThan(321);
        criteriaLI.withNumberProperty("OFFSET").thatIsGreaterThan(1);
        final List<Material> materialsLI = searchMaterials(sessionToken, criteriaLI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLI, "913_A (SIRNA)", "XX333_B (SIRNA)", "XX444_A (SIRNA)");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchForMaterialWithDatePropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createADatePropertyType(sessionToken, "DATE");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("DATE_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("DATE", "2020-02-09");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaContainsMatch = new MaterialSearchCriteria();
        criteriaContainsMatch.withProperty("DATE").thatContains("02");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaContainsMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "DATE"));

        final MaterialSearchCriteria criteriaStartsWithMatch = new MaterialSearchCriteria();
        criteriaStartsWithMatch.withProperty("DATE").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaStartsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "DATE"));

        final MaterialSearchCriteria criteriaEndsWithMatch = new MaterialSearchCriteria();
        criteriaEndsWithMatch.withProperty("DATE").thatEndsWith("09");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaEndsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "DATE"));
    }

    @Test
    public void testSearchForMaterialWithTimestampPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createATimestampPropertyType(sessionToken, "TIMESTAMP");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("TIMESTAMP_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("TIMESTAMP", "2020-02-09 10:00:00 +0100");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaContainsMatch = new MaterialSearchCriteria();
        criteriaContainsMatch.withProperty("TIMESTAMP").thatContains("20");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaContainsMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "TIMESTAMP"));

        final MaterialSearchCriteria criteriaStartsWithMatch = new MaterialSearchCriteria();
        criteriaStartsWithMatch.withProperty("TIMESTAMP").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaStartsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "TIMESTAMP"));

        final MaterialSearchCriteria criteriaEndsWithMatch = new MaterialSearchCriteria();
        criteriaEndsWithMatch.withProperty("TIMESTAMP").thatEndsWith("0100");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaEndsWithMatch, new MaterialFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "TIMESTAMP"));
    }

    @Test
    public void testSearchForMaterialWithStringPropertyQueriedAsIntegerOrDate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("SHORT_TEXT_PROPERTY_TEST");
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty("SHORT_TEXT", "123");

        v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation));

        final MaterialSearchCriteria criteriaWithNumberProperty = new MaterialSearchCriteria();
        criteriaWithNumberProperty.withNumberProperty("SHORT_TEXT").thatEquals(123);
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaWithNumberProperty, new MaterialFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "NumberPropertySearchCriteria", "VARCHAR"));

        final MaterialSearchCriteria criteriaWithDateProperty = new MaterialSearchCriteria();
        criteriaWithDateProperty.withDateProperty("SHORT_TEXT").thatEquals("1990-11-09");
        assertUserFailureException(
                Void -> searchMaterials(sessionToken, criteriaWithDateProperty, new MaterialFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "DatePropertySearchCriteria", "VARCHAR"));
    }

    @DataProvider
    protected Object[][] withPropertyExamples()
    {
        return new Object[][] {
                { DataType.VARCHAR, "12", "== 12", true },
                { DataType.VARCHAR, "ab", "<= abc", true },
                { DataType.VARCHAR, "12", "> 100", true },
                { DataType.VARCHAR, "ac3", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "abc3", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "ab34", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "abc34", "contains bc and endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "ac3", "contains bc or endsWith 4", false },
                { DataType.MULTILINE_VARCHAR, "abc3", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "ab34", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "abc34", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "12", "> 100 and <= 13", true },
                { DataType.BOOLEAN, "true", "== true", true },
                { DataType.BOOLEAN, "true", "== false", false },
                { DataType.BOOLEAN, "false", "== true", false },
                { DataType.BOOLEAN, "false", "== false", true },
        };
    }

    @Test(dataProvider = "withPropertyExamples")
    public void testWithProperty(final DataType dataType, final String value, final String queryString,
            final boolean found)
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        final MaterialPermId entityPermId = createMaterial(sessionToken, propertyTypeId, value);
        final MaterialSearchCriteria searchCriteria = new MaterialSearchCriteria();
        new AbstractSearchPropertyTest.StringQueryInjector(searchCriteria, propertyTypeId, false)
                .buildCriteria(queryString);

        // When
        final List<? extends IPermIdHolder> entities = searchMaterials(sessionToken, searchCriteria,
                new MaterialFetchOptions());

        // Then
        assertEquals(entities.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(entities.get(0).getPermId().toString(), entityPermId.toString());
        }
    }

    @DataProvider
    protected Object[][] withDateOrTimestampPropertyExamples()
    {
        return new Object[][] {
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "== 2020-02-15", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "== 2020-02-14", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), ">= 2020-02-16", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), ">= 2020-02-15", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), ">= 2020-02-14", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "<= 2020-02-16", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "<= 2020-02-15", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "<= 2020-02-14", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "> 2020-02-16", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "> 2020-02-15", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "> 2020-02-14", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "< 2020-02-16", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "< 2020-02-15", false },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "< 2020-02-14", false },

                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "== 2020-02-15 10:00:01", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "== 2020-02-15 10:00:00", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), ">= 2020-02-15 10:00:02", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), ">= 2020-02-15 10:00:01", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), ">= 2020-02-15 10:00:00", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "<= 2020-02-15 10:00:02", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "<= 2020-02-15 10:00:01", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "<= 2020-02-15 10:00:00", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "> 2020-02-15 10:00:02", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "> 2020-02-15 10:00:01", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "> 2020-02-15 10:00:00", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "< 2020-02-15 10:00:02", true },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "< 2020-02-15 10:00:01", false },
                { DataType.TIMESTAMP, createDate(2020, Calendar.FEBRUARY, 15, 10, 0, 1), "< 2020-02-15 10:00:00", false },
        };
    }

    @Test(dataProvider = "withDateOrTimestampPropertyExamples")
    public void testWithDateOrTimestampProperty(final DataType dataType, final Date value, final String queryString,
            final boolean found)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        final DateFormat dateFormat = dataType == DataType.DATE ? DATE_FORMAT : DATE_HOURS_MINUTES_SECONDS_FORMAT;
        final String formattedValue = dateFormat.format(value);
        final MaterialPermId entityPermId = createMaterial(sessionToken, propertyTypeId, formattedValue);
        final MaterialFetchOptions emptyFetchOptions = new MaterialFetchOptions();

        // Given
        final MaterialSearchCriteria dateSearchCriteria = new MaterialSearchCriteria();
        new AbstractSearchPropertyTest.DateQueryInjector(dateSearchCriteria, propertyTypeId, dateFormat)
                .buildCriteria(queryString);

        // When
        final List<? extends IPermIdHolder> dateEntities = searchMaterials(sessionToken, dateSearchCriteria,
                emptyFetchOptions);

        // Then
        assertEquals(dateEntities.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(dateEntities.get(0).getPermId().toString(), entityPermId.toString());
        }

        // Given
        final MaterialSearchCriteria dateSearchStringPropertyCriteria = new MaterialSearchCriteria();
        new AbstractSearchPropertyTest.StringQueryInjector(dateSearchStringPropertyCriteria, propertyTypeId, false)
                .buildCriteria(queryString);

        // When
        final List<? extends IPermIdHolder> dateEntitiesFromStringPropertyCriteria = searchMaterials(sessionToken,
                dateSearchStringPropertyCriteria, emptyFetchOptions);

        // Then
        assertEquals(dateEntitiesFromStringPropertyCriteria.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(dateEntitiesFromStringPropertyCriteria.get(0).getPermId().toString(), entityPermId.toString());
        }

        if (dataType == DataType.TIMESTAMP)
        {
            // Given
            final MaterialSearchCriteria timestampSearchCriteria = new MaterialSearchCriteria();
            new AbstractSearchPropertyTest.DateQueryInjector(timestampSearchCriteria, propertyTypeId, null)
                    .buildCriteria(queryString);

            // When
            final List<? extends IPermIdHolder> timestampEntities = searchMaterials(sessionToken,
                    timestampSearchCriteria, emptyFetchOptions);

            // Then
            assertEquals(timestampEntities.size(), found ? 1 : 0);
            if (found)
            {
                assertEquals(timestampEntities.get(0).getPermId().toString(), entityPermId.toString());
            }

            // Given
            final MaterialSearchCriteria timestampSearchStringPropertyCriteria = new MaterialSearchCriteria();
            new AbstractSearchPropertyTest.StringQueryInjector(timestampSearchStringPropertyCriteria, propertyTypeId,
                    false).buildCriteria(queryString);

            // When
            final List<? extends IPermIdHolder> timestampEntitiesFromStringPropertyCriteria =
                    searchMaterials(sessionToken, timestampSearchStringPropertyCriteria, emptyFetchOptions);

            // Then
            assertEquals(timestampEntitiesFromStringPropertyCriteria.size(), found ? 1 : 0);
            if (found)
            {
                assertEquals(timestampEntitiesFromStringPropertyCriteria.get(0).getPermId().toString(),
                        entityPermId.toString());
            }
        }
    }

    @DataProvider
    protected Object[][] withControlledVocabularyPropertyExamples()
    {
        return new Object[][] {
                { "WINTER", "== WINTER", true },
                { "WINTER", "== SUMMER", false },
                { "WINTER", "<= WINTER", true },
                { "SUMMER", "<= WINTER", true },
                { "WINTER", "<= SUMMER", false },
                { "WINTER", "< WINTER", false },
                { "SUMMER", "< WINTER", true },
                { "WINTER", "< SUMMER", false },
                { "WINTER", ">= WINTER", true },
                { "WINTER", ">= SUMMER", true },
                { "SUMMER", ">= WINTER", false },
                { "WINTER", "> WINTER", false },
                { "WINTER", "> SUMMER", true },
                { "SUMMER", "> WINTER", false },

                { "WINTER", "contains I and endsWith ER", true },
                { "SUMMER", "contains I and endsWith ER", false },
                { "SPRING", "contains I and endsWith ER", false },
                { "SUMMER", "startsWith SU", true },
                { "SPRING", "startsWith SU", false },
        };
    }

    @Test(dataProvider = "withControlledVocabularyPropertyExamples")
    public void testWithControlledVocabularyProperty(final String value, final String queryString, final boolean found)
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final VocabularyTermCreation vocabularyTermCreation1 = new VocabularyTermCreation();
        vocabularyTermCreation1.setCode("WINTER");
        final VocabularyTermCreation vocabularyTermCreation2 = new VocabularyTermCreation();
        vocabularyTermCreation2.setCode("SPRING");
        final VocabularyTermCreation vocabularyTermCreation3 = new VocabularyTermCreation();
        vocabularyTermCreation3.setCode("SUMMER");
        final VocabularyTermCreation vocabularyTermCreation4 = new VocabularyTermCreation();
        vocabularyTermCreation4.setCode("AUTUMN");

        final VocabularyCreation vocabularyCreation = new VocabularyCreation();
        vocabularyCreation.setCode("SEASONS");
        vocabularyCreation.setTerms(Arrays.asList(vocabularyTermCreation1, vocabularyTermCreation2,
                vocabularyTermCreation3, vocabularyTermCreation4));
        final VocabularyPermId vocabularyPermId =
                v3api.createVocabularies(sessionToken, Collections.singletonList(vocabularyCreation)).get(0);

        final PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, DataType.CONTROLLEDVOCABULARY,
                vocabularyPermId);
        final MaterialPermId entityPermId = createMaterial(sessionToken, propertyTypeId, value);
        final MaterialSearchCriteria searchCriteria = new MaterialSearchCriteria();
        new AbstractSearchPropertyTest.StringQueryInjector(searchCriteria, propertyTypeId, false)
                .buildCriteria(queryString);

        // When
        final List<? extends IPermIdHolder> entities = searchMaterials(sessionToken, searchCriteria,
                new MaterialFetchOptions());

        // Then
        assertEquals(entities.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(entities.get(0).getPermId().toString(), entityPermId.toString());
        }
    }

    private MaterialPermId createMaterial(final String sessionToken, final PropertyTypePermId propertyType,
            final String formattedValue)
    {
        final EntityTypePermId materialType = createAMaterialType(sessionToken, false, propertyType);

        final MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("TEST-MATERIAL-" + System.currentTimeMillis());
        materialCreation.setTypeId(materialType);
        materialCreation.setProperty(propertyType.getPermId(), formattedValue);

        return v3api.createMaterials(sessionToken, Collections.singletonList(materialCreation)).get(0);
    }

    private List<Material> searchMaterials(final String sessionToken, final MaterialSearchCriteria criteria,
            final MaterialFetchOptions options)
    {
        return v3api.searchMaterials(sessionToken, criteria, options).getObjects();
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, MaterialFetchOptions options, MaterialPermId... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, options);
        List<Material> materials = searchResult.getObjects();

        assertMaterialPermIds(materials, expectedPermIds);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, MaterialPermId... expectedPermIds)
    {
        testSearch(user, criteria, new MaterialFetchOptions(), expectedPermIds);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, new MaterialFetchOptions());
        List<Material> materials = searchResult.getObjects();

        assertEquals(materials.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
