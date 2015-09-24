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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;

/**
 * @author pkupczyk
 */
public class SearchExperimentTest extends AbstractExperimentTest
{
    @Test
    public void testSearchWithAttachments()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        AttachmentFetchOptions previousVersionAttachmentOptions = fo.withAttachments().withPreviousVersion();
        previousVersionAttachmentOptions.withContent();
        previousVersionAttachmentOptions.withRegistrator();
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        List<Experiment> experiments = searchExperiments(sessionToken, criteria, fo);
        v3api.logout(sessionToken);
        
        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getFetchOptions().hasAttachments(), true);
        List<Attachment> attachments = experiment.getAttachments();
        Attachment attachment = attachments.get(0);
        assertEquals(attachment.getVersion(), new Integer(4));
        assertEquals(attachment.getFileName(), "exampleExperiments.txt");
        assertEquals(attachment.getTitle(), "Latest version");
        assertEquals(attachment.getDescription(), null);
        assertEquals(attachment.getFetchOptions().hasContent(), false);
        assertEquals(attachment.getFetchOptions().hasRegistrator(), false);
        assertEquals(attachment.getFetchOptions().hasPreviousVersion(), true);
        Attachment previousVersion = attachment.getPreviousVersion();
        assertEquals(previousVersion.getVersion(), new Integer(3));
        assertEquals(previousVersion.getFileName(), "exampleExperiments.txt");
        assertEquals(previousVersion.getTitle(), null);
        assertEquals(previousVersion.getDescription(), "Second latest version");
        assertEquals(previousVersion.getFetchOptions().hasRegistrator(), true);
        assertEquals(previousVersion.getRegistrator().toString(), "Person test");
        assertEquals(previousVersion.getRegistrationDate().toString(), "2008-12-10 13:49:20.23603");
        assertEquals(previousVersion.getFetchOptions().hasContent(), true);
        assertEquals(previousVersion.getFetchOptions().hasPreviousVersion(), false);
        assertEquals(previousVersion.getContent().length, 227);
        assertEquals(attachments.size(), 1);
        assertEquals(experiments.size(), 1);
    }


    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withId().thatEquals(new ExperimentPermId("200811050951882-1028"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withCode().thatStartsWith("EXP1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithProjectWithIdSetToIdentifier()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withPermId().thatEquals("20120814110011738-106");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criteria, "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithIdSetToPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, expected);

        criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criteria, expected);
    }

    @Test
    public void testSearchWithProjectWithSpaceWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, expected);

        criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, expected);
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("desc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("esc1");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("desc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("esc1");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("desc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("esc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithProperty()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("COMMENT");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("desc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("esc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("esc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithDatePropertyThatEqualsWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2009-02-08");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(2).thatEquals("2009-02-10");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatEqualsWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-08 23:59"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-09 00:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-09 23:59"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-10 00:00"));
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithDatePropertyThatIsEarlierThanOrEqualToWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-08");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(1).thatIsEarlierThanOrEqualTo("2009-02-09 09:00");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-09 09:00");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(2).thatIsEarlierThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatEarlierThanOrEqualToWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 08:59"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 09:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 23:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatIsLaterThanOrEqualToWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-10");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-08");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatIsLaterThanOrEqualToWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-10 00:01"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-10 00:00"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-09 10:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyPropertyThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEquals("FEMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEquals("FEMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEqualsWithWildcards()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEquals("*EMAL*");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyPropertyThatStartsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("FEMAL");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("EMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEndsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEndsWith("EMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEndsWith("EMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatContains()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatContains("EMAL");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatContains("FMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("FEMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("EXP-TEST-2");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithRegistrationDateThatIsLaterThan()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, 5);
    }

    @Test
    public void testSearchWithRegistrationDateThatIsEarlierThan()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05");
        testSearch(TEST_USER, criteria, 7);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModificationDate().thatEquals("2009-03-18");
        testSearch(TEST_USER, criteria, 12);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050952663-1029");
        criteria.withPermId().thatEquals("200811050952663-1030");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criteria, 1);

        criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    private void testSearch(String user, ExperimentSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Experiment> experiments = searchExperiments(sessionToken, criteria, new ExperimentFetchOptions());

        assertExperimentIdentifiers(experiments, expectedIdentifiers);
    }

    private void testSearch(String user, ExperimentSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Experiment> experiments = searchExperiments(sessionToken, criteria, new ExperimentFetchOptions());

        assertEquals(experiments.size(), expectedCount);
    }


    private List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria criteria, 
            ExperimentFetchOptions fetchOptions)
    {
        SearchResult<Experiment> searchResult = v3api.searchExperiments(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return searchResult.getObjects();
    }

}
