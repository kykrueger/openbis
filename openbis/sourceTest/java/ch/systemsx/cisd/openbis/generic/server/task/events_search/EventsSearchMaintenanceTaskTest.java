package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.shared.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.transaction.support.TransactionCallback;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.assertEquals;

@Test
public class EventsSearchMaintenanceTaskTest
{

    private static final String INFO_PREFIX = "INFO  OPERATION." + EventsSearchMaintenanceTask.class.getSimpleName() + " - ";

    private static final String ERROR_PREFIX = "ERROR OPERATION." + EventsSearchMaintenanceTask.class.getSimpleName() + " - ";

    private static final DateFormat DATE_TIME_MILLIS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private BufferedAppender logRecorder;

    private Mockery mockery;

    private final TestResources resources = new TestResources(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private IDataSource dataSource;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        mockery = new Mockery();
        dataSource = mockery.mock(IDataSource.class);

        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).executeInNewTransaction(with(any(TransactionCallback.class)));
                will(new CustomAction("execute callback")
                {
                    @Override public Object invoke(Invocation invocation) throws Throwable
                    {
                        ((TransactionCallback) invocation.getParameter(0)).doInTransaction(null);
                        return null;
                    }
                });
            }
        });
    }

    @AfterMethod
    public void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testSpaces()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - delete space A
        // - create space A
        // - delete space A
        // - delete space B
        // - create space A

        PersonPE deleterA1 = new PersonPE();
        deleterA1.setUserId("deleter_A1");

        PersonPE deleterA2 = new PersonPE();
        deleterA2.setUserId("deleter_A2");

        PersonPE deleterB = new PersonPE();
        deleterB.setUserId("deleter_B");

        EventPE deletionA1 = new EventPE();
        deletionA1.setId(1L);
        deletionA1.setEventType(EventType.DELETION);
        deletionA1.setEntityType(EntityType.SPACE);
        deletionA1.setIdentifiers(Collections.singletonList("A"));
        deletionA1.setDescription("Description A1");
        deletionA1.setReason("Reason A1");
        deletionA1.setRegistrator(deleterA1);
        deletionA1.setRegistrationDate(dateTimeMillis("2000-01-01 01:00:00.000"));

        EventPE deletionA2 = new EventPE();
        deletionA2.setId(2L);
        deletionA2.setEventType(EventType.DELETION);
        deletionA2.setEntityType(EntityType.SPACE);
        deletionA2.setIdentifiers(Collections.singletonList("A"));
        deletionA2.setDescription("Description A2");
        deletionA2.setReason("Reason A2");
        deletionA2.setRegistrator(deleterA2);
        deletionA2.setRegistrationDate(dateTimeMillis("2000-01-01 02:00:00.000"));

        EventPE deletionB = new EventPE();
        deletionB.setId(3L);
        deletionB.setEventType(EventType.DELETION);
        deletionB.setEntityType(EntityType.SPACE);
        deletionB.setIdentifiers(Collections.singletonList("B"));
        deletionB.setDescription("Description B");
        deletionB.setReason("Reason B");
        deletionB.setRegistrator(deleterB);
        deletionB.setRegistrationDate(dateTimeMillis("2000-01-01 03:00:00.000"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("A");
        spaceA.setRegistrationDate(dateTimeMillis("2000-01-01 10:00:00.000"));

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadLastTimestampsEmpty(EventType.FREEZING, EventType.MOVEMENT);
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2000-01-01 00:30:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2000-01-01 00:29:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2000-01-01 00:28:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2000-01-01 00:27:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2000-01-01 00:26:00.000"));

            expectLoadEvents(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2000-01-01 00:26:00.000"), deletionA1, deletionA2);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionA2.getRegistrationDateInternal(), deletionB);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionB.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2000-01-01 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2000-01-01 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2000-01-01 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2000-01-01 00:26:00.000"));

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL, EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                Date randomTimestamp = new Date((long) (Math.random() * 10000));
                expectLoadLastTimestamp(EventType.DELETION, entityType, randomTimestamp);
                expectLoadEvents(EventType.DELETION, entityType, randomTimestamp);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionA1Expected = createExpectedEvent(deletionA1);
        deletionA1Expected.setEntitySpace("A");
        deletionA1Expected.setIdentifier("A");
        assertExpectedEvent(events.get(0), deletionA1Expected);

        EventsSearchPE deletionA2Expected = createExpectedEvent(deletionA2);
        deletionA2Expected.setEntitySpace("A");
        deletionA2Expected.setIdentifier("A");
        assertExpectedEvent(events.get(1), deletionA2Expected);

        EventsSearchPE deletionBExpected = createExpectedEvent(deletionB);
        deletionBExpected.setEntitySpace("B");
        deletionBExpected.setIdentifier("B");
        assertExpectedEvent(events.get(2), deletionBExpected);
    }

    @Test
    public void testProjects()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - move project /A/A to space B
        // - delete space A
        // - delete project /B/A
        // - delete space B
        // - create space C
        // - create project /C/B
        // - delete project /C/B

        PersonPE deleterSpaceA = new PersonPE();
        deleterSpaceA.setUserId("deleter_space_A");

        PersonPE deleterSpaceB = new PersonPE();
        deleterSpaceB.setUserId("deleter_space_B");

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        PersonPE deleterProjectB = new PersonPE();
        deleterProjectB.setUserId("deleter_project_B");

        EventPE deletionSpaceA = new EventPE();
        deletionSpaceA.setId(1L);
        deletionSpaceA.setEventType(EventType.DELETION);
        deletionSpaceA.setEntityType(EntityType.SPACE);
        deletionSpaceA.setIdentifiers(Collections.singletonList("SPACE_A"));
        deletionSpaceA.setDescription("Description Space A");
        deletionSpaceA.setReason("Reason Space A");
        deletionSpaceA.setRegistrator(deleterSpaceA);
        deletionSpaceA.setRegistrationDate(dateTimeMillis("2021-03-29 11:44:12.905"));

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(2L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210329114338970-205170"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testProjects_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-03-29 11:44:46.175"));

        EventPE deletionSpaceB = new EventPE();
        deletionSpaceB.setId(3L);
        deletionSpaceB.setEventType(EventType.DELETION);
        deletionSpaceB.setEntityType(EntityType.SPACE);
        deletionSpaceB.setIdentifiers(Collections.singletonList("SPACE_B"));
        deletionSpaceB.setDescription("Description Space B");
        deletionSpaceB.setReason("Reason Space B");
        deletionSpaceB.setRegistrator(deleterSpaceB);
        deletionSpaceB.setRegistrationDate(dateTimeMillis("2021-03-29 11:44:56.557"));

        EventPE deletionProjectB = new EventPE();
        deletionProjectB.setId(4L);
        deletionProjectB.setEventType(EventType.DELETION);
        deletionProjectB.setEntityType(EntityType.PROJECT);
        deletionProjectB.setIdentifiers(Collections.singletonList("20210329115117300-205171"));
        deletionProjectB.setDescription("Description Project B");
        deletionProjectB.setReason("Reason Project B");
        deletionProjectB.setContent(loadFile("testProjects_deletionProjectB.json"));
        deletionProjectB.setRegistrator(deleterProjectB);
        deletionProjectB.setRegistrationDate(dateTimeMillis("2021-03-29 11:51:28.866"));

        SpacePE spaceC = new SpacePE();
        spaceC.setId(100L);
        spaceC.setCode("SPACE_C");
        spaceC.setRegistrationDate(dateTimeMillis("2021-03-29 11:51:04.772"));

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadLastTimestampsEmpty(EventType.MOVEMENT, EventType.FREEZING);
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-03-29 00:30:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-03-29 00:29:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-03-29 00:28:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-03-29 00:27:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-03-29 00:26:00.000"));

            expectLoadEvents(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-03-29 00:26:00.000"), deletionSpaceA, deletionSpaceB);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionSpaceB.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-03-29 00:26:00.000"), deletionProjectA);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectA.getRegistrationDateInternal(), deletionProjectB);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectB.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-03-29 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-03-29 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-03-29 00:26:00.000"));

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL, EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                Date randomTimestamp = new Date((long) (Math.random() * 10000));
                expectLoadLastTimestamp(EventType.DELETION, entityType, randomTimestamp);
                expectLoadEvents(EventType.DELETION, entityType, randomTimestamp);
            }

            expectLoadSpaces(spaceC);

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 4);

        EventsSearchPE deletionSpaceAExpected = createExpectedEvent(deletionSpaceA);
        deletionSpaceAExpected.setEntitySpace("SPACE_A");
        deletionSpaceAExpected.setIdentifier("SPACE_A");
        assertExpectedEvent(events.get(0), deletionSpaceAExpected);

        EventsSearchPE deletionSpaceBExpected = createExpectedEvent(deletionSpaceB);
        deletionSpaceBExpected.setEntitySpace("SPACE_B");
        deletionSpaceBExpected.setIdentifier("SPACE_B");
        assertExpectedEvent(events.get(1), deletionSpaceBExpected);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_B");
        deletionProjectAExpected.setEntityProject("/SPACE_B/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210329114338970-205170");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 11:43:38.000"));
        deletionProjectAExpected.setIdentifier("20210329114338970-205170");
        deletionProjectAExpected.setContent(loadFile("testProjects_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(2), deletionProjectAExpected);

        EventsSearchPE deletionProjectBExpected = createExpectedEvent(deletionProjectB);
        deletionProjectBExpected.setEntitySpace("SPACE_C");
        deletionProjectBExpected.setEntitySpacePermId("100");
        deletionProjectBExpected.setEntityProject("/SPACE_C/PROJECT_B");
        deletionProjectBExpected.setEntityProjectPermId("20210329115117300-205171");
        deletionProjectBExpected.setEntityRegisterer("registerer_project_B");
        deletionProjectBExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 11:51:17.000"));
        deletionProjectBExpected.setIdentifier("20210329115117300-205171");
        deletionProjectBExpected.setContent(loadFile("testProjects_deletionProjectBExpected.json"));
        assertExpectedEvent(events.get(3), deletionProjectBExpected);
    }

    @Test
    public void testExperiments()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create project /B/B
        // - create experiment /A/A/A
        // - move experiment /A/A/A to project /B/B (the experiment becomes /B/B/A)
        // - delete project /A/A
        // - move project /B/B to space A (the project becomes /A/B and the experiment becomes /A/B/A)
        // - delete experiment /A/B/A
        // - delete project /A/B
        // - create project /A/A
        // - create experiment /A/A/A
        // - create experiment /A/A/B
        // - delete experiments /A/A/A and /A/A/B

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        PersonPE deleterProjectB = new PersonPE();
        deleterProjectB.setUserId("deleter_project_B");

        PersonPE deleterExperimentsAB = new PersonPE();
        deleterExperimentsAB.setUserId("deleter_experiments_AB");

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(1L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210329150838023-205177"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testExperiments_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-03-29 15:09:43.362"));

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(2L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210329150903348-205179"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testExperiments_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-03-29 15:10:36.969"));

        EventPE deletionProjectB = new EventPE();
        deletionProjectB.setId(3L);
        deletionProjectB.setEventType(EventType.DELETION);
        deletionProjectB.setEntityType(EntityType.PROJECT);
        deletionProjectB.setIdentifiers(Collections.singletonList("20210329150846687-205178"));
        deletionProjectB.setDescription("Description Project B");
        deletionProjectB.setReason("Reason Project B");
        deletionProjectB.setContent(loadFile("testExperiments_deletionProjectB.json"));
        deletionProjectB.setRegistrator(deleterProjectB);
        deletionProjectB.setRegistrationDate(dateTimeMillis("2021-03-29 15:10:48.428"));

        EventPE deletionExperimentsAB = new EventPE();
        deletionExperimentsAB.setId(4L);
        deletionExperimentsAB.setEventType(EventType.DELETION);
        deletionExperimentsAB.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentsAB.setIdentifiers(Arrays.asList("20210329151120278-205181", "20210329151130167-205182"));
        deletionExperimentsAB.setDescription("Description Experiments A and B");
        deletionExperimentsAB.setReason("Reason Experiments A and B");
        deletionExperimentsAB.setContent(loadFile("testExperiments_deletionExperimentsAB.json"));
        deletionExperimentsAB.setRegistrator(deleterExperimentsAB);
        deletionExperimentsAB.setRegistrationDate(dateTimeMillis("2021-03-29 15:11:51.035"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("SPACE_A");
        spaceA.setRegistrationDate(dateTimeMillis("2021-03-29 15:07:55.947"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-03-29 15:08:03.609"));

        Space spaceAv3 = new Space();
        spaceAv3.setCode("SPACE_A");

        ProjectFetchOptions projectFo = new ProjectFetchOptions();
        projectFo.withSpace();
        projectFo.withHistory();

        Project projectA = new Project();
        projectA.setCode("PROJECT_A");
        projectA.setPermId(new ProjectPermId("20210329151103947-205180"));
        projectA.setSpace(spaceAv3);
        projectA.setRegistrationDate(dateTimeMillis("2021-03-29 15:11:03.947"));
        projectA.setHistory(Collections.emptyList());
        projectA.setFetchOptions(projectFo);

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadLastTimestampsEmpty(EventType.MOVEMENT, EventType.FREEZING);
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-03-29 00:30:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-03-29 00:29:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-03-29 00:28:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-03-29 00:27:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-03-29 00:26:00.000"));

            expectLoadEvents(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-03-29 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-03-29 00:26:00.000"), deletionProjectA, deletionProjectB);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectB.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-03-29 00:26:00.000"), deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal(), deletionExperimentsAB);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentsAB.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-03-29 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-03-29 00:26:00.000"));

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL, EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                Date randomTimestamp = new Date((long) (Math.random() * 10000));
                expectLoadLastTimestamp(EventType.DELETION, entityType, randomTimestamp);
                expectLoadEvents(EventType.DELETION, entityType, randomTimestamp);
            }

            expectLoadSpaces(spaceA, spaceB);
            expectLoadProjects(projectA);

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 5);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_A");
        deletionProjectAExpected.setEntitySpacePermId("100");
        deletionProjectAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210329150838023-205177");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 15:08:38.000"));
        deletionProjectAExpected.setIdentifier("20210329150838023-205177");
        deletionProjectAExpected.setContent(loadFile("testExperiments_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(0), deletionProjectAExpected);

        EventsSearchPE deletionProjectBExpected = createExpectedEvent(deletionProjectB);
        deletionProjectBExpected.setEntitySpace("SPACE_A");
        deletionProjectBExpected.setEntitySpacePermId("100");
        deletionProjectBExpected.setEntityProject("/SPACE_A/PROJECT_B");
        deletionProjectBExpected.setEntityProjectPermId("20210329150846687-205178");
        deletionProjectBExpected.setEntityRegisterer("registerer_project_B");
        deletionProjectBExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 15:08:46.000"));
        deletionProjectBExpected.setIdentifier("20210329150846687-205178");
        deletionProjectBExpected.setContent(loadFile("testExperiments_deletionProjectBExpected.json"));
        assertExpectedEvent(events.get(1), deletionProjectBExpected);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_A");
        deletionExperimentAExpected.setEntitySpacePermId("100");
        deletionExperimentAExpected.setEntityProject("/SPACE_A/PROJECT_B");
        deletionExperimentAExpected.setEntityProjectPermId("20210329150846687-205178");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 15:09:03.000"));
        deletionExperimentAExpected.setIdentifier("20210329150903348-205179");
        deletionExperimentAExpected.setContent(loadFile("testExperiments_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(2), deletionExperimentAExpected);

        EventsSearchPE deletionExperimentsABExpectedB = createExpectedEvent(deletionExperimentsAB);
        deletionExperimentsABExpectedB.setEntitySpace("SPACE_A");
        deletionExperimentsABExpectedB.setEntitySpacePermId("100");
        deletionExperimentsABExpectedB.setEntityProject("/SPACE_A/PROJECT_A");
        deletionExperimentsABExpectedB.setEntityProjectPermId("20210329151103947-205180");
        deletionExperimentsABExpectedB.setEntityRegisterer("registerer_experiment_B");
        deletionExperimentsABExpectedB.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 15:11:30.000"));
        deletionExperimentsABExpectedB.setIdentifier("20210329151130167-205182");
        deletionExperimentsABExpectedB.setContent(loadFile("testExperiments_deletionExperimentsABExpectedB.json"));
        assertExpectedEvent(events.get(3), deletionExperimentsABExpectedB);

        EventsSearchPE deletionExperimentsABExpectedA = createExpectedEvent(deletionExperimentsAB);
        deletionExperimentsABExpectedA.setEntitySpace("SPACE_A");
        deletionExperimentsABExpectedA.setEntitySpacePermId("100");
        deletionExperimentsABExpectedA.setEntityProject("/SPACE_A/PROJECT_A");
        deletionExperimentsABExpectedA.setEntityProjectPermId("20210329151103947-205180");
        deletionExperimentsABExpectedA.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentsABExpectedA.setEntityRegistrationTimestamp(dateTimeMillis("2021-03-29 15:11:20.000"));
        deletionExperimentsABExpectedA.setIdentifier("20210329151120278-205181");
        deletionExperimentsABExpectedA.setContent(loadFile("testExperiments_deletionExperimentsABExpectedA.json"));
        assertExpectedEvent(events.get(4), deletionExperimentsABExpectedA);
    }

    @Test
    public void testExperimentWithUnknownSpace()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create experiment /A/A/A
        // - delete experiment /A/A/A
        // - move project /A/A to space B (it becomes /B/A)
        // - delete space A (project /B/A relation with space A loses space_id and will be stored as UNKNOWN)
        // - delete project /B/A

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        PersonPE deleterSpaceA = new PersonPE();
        deleterSpaceA.setUserId("deleter_space_A");

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(1L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210419162809496-205242"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testExperimentWithUnknownSpace_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-04-19 16:28:27.497"));

        EventPE deletionSpaceA = new EventPE();
        deletionSpaceA.setId(2L);
        deletionSpaceA.setEventType(EventType.DELETION);
        deletionSpaceA.setEntityType(EntityType.SPACE);
        deletionSpaceA.setIdentifiers(Collections.singletonList("SPACE_A"));
        deletionSpaceA.setDescription("Description Space A");
        deletionSpaceA.setReason("Reason Space A");
        deletionSpaceA.setRegistrator(deleterSpaceA);
        deletionSpaceA.setRegistrationDate(dateTimeMillis("2021-04-19 16:29:52.293"));

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(3L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210419162753803-205241"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testExperimentWithUnknownSpace_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-04-19 16:30:11.575"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-04-19 16:27:27.930"));

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadSpaces(spaceB);
            expectLoadProjects();
            expectLoadLastTimestampsEmpty(EventType.values());
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, null, deletionSpaceA);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionSpaceA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, null, deletionProjectA);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, null, deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet
                    .of(EntityType.SAMPLE, EntityType.DATASET, EntityType.MATERIAL,
                            EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                            EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                expectLoadEvents(EventType.DELETION, entityType, null);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionSpaceAExpected = createExpectedEvent(deletionSpaceA);
        deletionSpaceAExpected.setEntitySpace("SPACE_A");
        deletionSpaceAExpected.setIdentifier("SPACE_A");
        assertExpectedEvent(events.get(0), deletionSpaceAExpected);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_B");
        deletionProjectAExpected.setEntitySpacePermId("200");
        deletionProjectAExpected.setEntityProject("/SPACE_B/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210419162753803-205241");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-19 16:27:53.000"));
        deletionProjectAExpected.setIdentifier("20210419162753803-205241");
        deletionProjectAExpected.setContent(loadFile("testExperimentWithUnknownSpace_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(1), deletionProjectAExpected);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_A");
        deletionExperimentAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionExperimentAExpected.setEntityProjectPermId("20210419162753803-205241");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-19 16:28:09.000"));
        deletionExperimentAExpected.setIdentifier("20210419162809496-205242");
        deletionExperimentAExpected.setContent(loadFile("testExperimentWithUnknownSpace_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(2), deletionExperimentAExpected);
    }

    @Test
    public void testSamples()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create project /B/B
        // - create experiment /A/A/A
        // - create experiment /B/B/B
        // - create sample A in space A
        // - move sample A to space B
        // - delete sample A
        // - create sample B in project /A/A
        // - move sample B to project /B/B
        // - delete sample B
        // - create sample C in experiment /A/A/A
        // - move sample C to experiment /B/B/B
        // - delete sample C
        // - create sample D in space A
        // - move sample D to experiment /A/A/A
        // - move experiment /A/A/A to project /B/B
        // - move sample D to experiment /B/B/B
        // - delete experiment /B/B/A
        // - delete sample D

        PersonPE deleterSampleA = new PersonPE();
        deleterSampleA.setUserId("deleter_sample_A");

        PersonPE deleterSampleB = new PersonPE();
        deleterSampleB.setUserId("deleter_sample_B");

        PersonPE deleterSampleC = new PersonPE();
        deleterSampleC.setUserId("deleter_sample_C");

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        PersonPE deleterSampleD = new PersonPE();
        deleterSampleD.setUserId("deleter_sample_D");

        EventPE deletionSampleA = new EventPE();
        deletionSampleA.setId(1L);
        deletionSampleA.setEventType(EventType.DELETION);
        deletionSampleA.setEntityType(EntityType.SAMPLE);
        deletionSampleA.setIdentifiers(Collections.singletonList("20210401151915172-205200"));
        deletionSampleA.setDescription("20210401151915172-205200");
        deletionSampleA.setReason("Reason Sample A");
        deletionSampleA.setContent(loadFile("testSamples_deletionSampleA.json"));
        deletionSampleA.setRegistrator(deleterSampleA);
        deletionSampleA.setRegistrationDate(dateTimeMillis("2021-04-01 15:20:49.605"));

        EventPE deletionSampleB = new EventPE();
        deletionSampleB.setId(2L);
        deletionSampleB.setEventType(EventType.DELETION);
        deletionSampleB.setEntityType(EntityType.SAMPLE);
        deletionSampleB.setIdentifiers(Collections.singletonList("20210401152005699-205201"));
        deletionSampleB.setDescription("20210401152005699-205201");
        deletionSampleB.setReason("Reason Sample B");
        deletionSampleB.setContent(loadFile("testSamples_deletionSampleB.json"));
        deletionSampleB.setRegistrator(deleterSampleB);
        deletionSampleB.setRegistrationDate(dateTimeMillis("2021-04-01 15:20:53.103"));

        EventPE deletionSampleC = new EventPE();
        deletionSampleC.setId(3L);
        deletionSampleC.setEventType(EventType.DELETION);
        deletionSampleC.setEntityType(EntityType.SAMPLE);
        deletionSampleC.setIdentifiers(Collections.singletonList("20210401152124191-205202"));
        deletionSampleC.setDescription("20210401152124191-205202");
        deletionSampleC.setReason("Reason Sample C");
        deletionSampleC.setContent(loadFile("testSamples_deletionSampleC.json"));
        deletionSampleC.setRegistrator(deleterSampleC);
        deletionSampleC.setRegistrationDate(dateTimeMillis("2021-04-01 15:21:55.391"));

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(4L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210401151830921-205198"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testSamples_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-04-01 15:23:42.574"));

        EventPE deletionSampleD = new EventPE();
        deletionSampleD.setId(5L);
        deletionSampleD.setEventType(EventType.DELETION);
        deletionSampleD.setEntityType(EntityType.SAMPLE);
        deletionSampleD.setIdentifiers(Collections.singletonList("20210401152213749-205203"));
        deletionSampleD.setDescription("20210401152213749-205203");
        deletionSampleD.setReason("Reason Sample D");
        deletionSampleD.setContent(loadFile("testSamples_deletionSampleD.json"));
        deletionSampleD.setRegistrator(deleterSampleD);
        deletionSampleD.setRegistrationDate(dateTimeMillis("2021-04-01 15:24:07.632"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("SPACE_A");
        spaceA.setRegistrationDate(dateTimeMillis("2021-03-29 15:07:55.947"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-03-29 15:08:03.609"));

        Space spaceAv3 = new Space();
        spaceAv3.setCode("SPACE_A");

        Space spaceBv3 = new Space();
        spaceBv3.setCode("SPACE_B");

        ProjectFetchOptions projectFo = new ProjectFetchOptions();
        projectFo.withSpace();
        projectFo.withHistory();

        Project projectA = new Project();
        projectA.setCode("PROJECT_A");
        projectA.setPermId(new ProjectPermId("20210329151103947-205180"));
        projectA.setSpace(spaceAv3);
        projectA.setRegistrationDate(dateTimeMillis("2021-03-29 15:11:03.947"));
        projectA.setHistory(Collections.emptyList());
        projectA.setFetchOptions(projectFo);

        Project projectB = new Project();
        projectB.setCode("PROJECT_B");
        projectB.setPermId(new ProjectPermId("20210401151815315-205197"));
        projectB.setSpace(spaceBv3);
        projectB.setRegistrationDate(dateTimeMillis("2021-04-01 15:18:15.315"));
        projectB.setHistory(Collections.emptyList());
        projectB.setFetchOptions(projectFo);

        ExperimentFetchOptions experimentFo = new ExperimentFetchOptions();
        experimentFo.withProject();
        experimentFo.withHistory();

        Experiment experimentB = new Experiment();
        experimentB.setCode("EXPERIMENT_B");
        experimentB.setPermId(new ExperimentPermId("20210401151838580-205199"));
        experimentB.setProject(projectB);
        experimentB.setRegistrationDate(dateTimeMillis("2021-04-01 15:18:38.580"));
        experimentB.setHistory(Collections.emptyList());
        experimentB.setFetchOptions(experimentFo);

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadLastTimestampsEmpty(EventType.MOVEMENT, EventType.FREEZING);
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-04-01 00:30:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-04-01 00:29:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-04-01 00:28:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-04-01 00:27:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-04-01 00:26:00.000"));

            expectLoadEvents(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-04-01 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-04-01 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-04-01 00:26:00.000"), deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-04-01 00:26:00.000"), deletionSampleA, deletionSampleB);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleB.getRegistrationDateInternal(), deletionSampleC, deletionSampleD);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleD.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-04-01 00:26:00.000"));

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL, EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                Date randomTimestamp = new Date((long) (Math.random() * 10000));
                expectLoadLastTimestamp(EventType.DELETION, entityType, randomTimestamp);
                expectLoadEvents(EventType.DELETION, entityType, randomTimestamp);
            }

            expectLoadSpaces(spaceA, spaceB);
            expectLoadProjects(projectA, projectB);
            expectLoadExperiments(experimentB);

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 5);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_B");
        deletionExperimentAExpected.setEntitySpacePermId("200");
        deletionExperimentAExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionExperimentAExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-01 15:18:30.000"));
        deletionExperimentAExpected.setIdentifier("20210401151830921-205198");
        deletionExperimentAExpected.setContent(loadFile("testSamples_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(0), deletionExperimentAExpected);

        EventsSearchPE deletionSampleAExpected = createExpectedEvent(deletionSampleA);
        deletionSampleAExpected.setEntitySpace("SPACE_B");
        deletionSampleAExpected.setEntitySpacePermId("200");
        deletionSampleAExpected.setEntityRegisterer("registerer_sample_A");
        deletionSampleAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-01 15:19:15.000"));
        deletionSampleAExpected.setIdentifier("20210401151915172-205200");
        deletionSampleAExpected.setContent(loadFile("testSamples_deletionSampleAExpected.json"));
        assertExpectedEvent(events.get(1), deletionSampleAExpected);

        EventsSearchPE deletionSampleBExpected = createExpectedEvent(deletionSampleB);
        deletionSampleBExpected.setEntitySpace("SPACE_B");
        deletionSampleBExpected.setEntitySpacePermId("200");
        deletionSampleBExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionSampleBExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionSampleBExpected.setEntityRegisterer("registerer_sample_B");
        deletionSampleBExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-01 15:20:05.000"));
        deletionSampleBExpected.setIdentifier("20210401152005699-205201");
        deletionSampleBExpected.setContent(loadFile("testSamples_deletionSampleBExpected.json"));
        assertExpectedEvent(events.get(2), deletionSampleBExpected);

        EventsSearchPE deletionSampleCExpected = createExpectedEvent(deletionSampleC);
        deletionSampleCExpected.setEntitySpace("SPACE_B");
        deletionSampleCExpected.setEntitySpacePermId("200");
        deletionSampleCExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionSampleCExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionSampleCExpected.setEntityRegisterer("registerer_sample_C");
        deletionSampleCExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-01 15:21:24.000"));
        deletionSampleCExpected.setIdentifier("20210401152124191-205202");
        deletionSampleCExpected.setContent(loadFile("testSamples_deletionSampleCExpected.json"));
        assertExpectedEvent(events.get(3), deletionSampleCExpected);

        EventsSearchPE deletionSampleDExpected = createExpectedEvent(deletionSampleD);
        deletionSampleDExpected.setEntitySpace("SPACE_B");
        deletionSampleDExpected.setEntitySpacePermId("200");
        deletionSampleDExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionSampleDExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionSampleDExpected.setEntityRegisterer("registerer_sample_D");
        deletionSampleDExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-01 15:22:13.000"));
        deletionSampleDExpected.setIdentifier("20210401152213749-205203");
        deletionSampleDExpected.setContent(loadFile("testSamples_deletionSampleDExpected.json"));
        assertExpectedEvent(events.get(4), deletionSampleDExpected);
    }

    @Test
    public void testSampleWithUnknownProject()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create project /B/B
        // - create experiment /A/A/A
        // - create sample A in experiment /A/A/A
        // - delete sample A
        // - move experiment /A/A/A to project /B/B (it becomes /B/B/A)
        // - delete project /A/A (experiment /B/B/A relation history with project /A/A loses proj_id and will be stored as UNKNOWN)
        // - delete experiment /B/B/A

        PersonPE deleterSampleA = new PersonPE();
        deleterSampleA.setUserId("deleter_sample_A");

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        EventPE deletionSampleA = new EventPE();
        deletionSampleA.setId(1L);
        deletionSampleA.setEventType(EventType.DELETION);
        deletionSampleA.setEntityType(EntityType.SAMPLE);
        deletionSampleA.setIdentifiers(Collections.singletonList("20210419173140752-205246"));
        deletionSampleA.setDescription("20210419173140752-205246");
        deletionSampleA.setReason("Reason Sample A");
        deletionSampleA.setContent(loadFile("testSampleWithUnknownProject_deletionSampleA.json"));
        deletionSampleA.setRegistrator(deleterSampleA);
        deletionSampleA.setRegistrationDate(dateTimeMillis("2021-04-19 17:32:01.964"));

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(2L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210419173048021-205243"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testSampleWithUnknownProject_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-04-19 17:32:47.361"));

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(3L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210419173116451-205245"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testSampleWithUnknownProject_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-04-19 17:34:27.090"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("SPACE_A");
        spaceA.setRegistrationDate(dateTimeMillis("2021-04-19 17:30:28.204"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-04-19 17:30:33.073"));

        Space spaceBv3 = new Space();
        spaceBv3.setCode("SPACE_B");

        ProjectFetchOptions projectFo = new ProjectFetchOptions();
        projectFo.withSpace();
        projectFo.withHistory();

        Project projectB = new Project();
        projectB.setCode("PROJECT_B");
        projectB.setPermId(new ProjectPermId("20210419173058839-205244"));
        projectB.setSpace(spaceBv3);
        projectB.setRegistrationDate(dateTimeMillis("2021-04-19 17:30:58.839"));
        projectB.setHistory(Collections.emptyList());
        projectB.setFetchOptions(projectFo);

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadSpaces(spaceA, spaceB);
            expectLoadProjects(projectB);
            expectLoadExperiments();
            expectLoadLastTimestampsEmpty(EventType.values());
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, null);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, null, deletionProjectA);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, null, deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, null, deletionSampleA);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleA.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet
                    .of(EntityType.DATASET, EntityType.MATERIAL,
                            EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                            EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                expectLoadEvents(EventType.DELETION, entityType, null);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_A");
        deletionProjectAExpected.setEntitySpacePermId("100");
        deletionProjectAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210419173048021-205243");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-19 17:30:48.000"));
        deletionProjectAExpected.setIdentifier("20210419173048021-205243");
        deletionProjectAExpected.setContent(loadFile("testSampleWithUnknownProject_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(0), deletionProjectAExpected);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_B");
        deletionExperimentAExpected.setEntitySpacePermId("200");
        deletionExperimentAExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionExperimentAExpected.setEntityProjectPermId("20210419173058839-205244");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-19 17:31:16.000"));
        deletionExperimentAExpected.setIdentifier("20210419173116451-205245");
        deletionExperimentAExpected.setContent(loadFile("testSampleWithUnknownProject_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(1), deletionExperimentAExpected);

        EventsSearchPE deletionSampleAExpected = createExpectedEvent(deletionSampleA);
        deletionSampleAExpected.setEntitySpace("SPACE_A");
        deletionSampleAExpected.setEntitySpacePermId("100");
        deletionSampleAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionSampleAExpected.setEntityProjectPermId("20210419173048021-205243");
        deletionSampleAExpected.setEntityRegisterer("registerer_sample_A");
        deletionSampleAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-19 17:31:40.000"));
        deletionSampleAExpected.setIdentifier("20210419173140752-205246");
        deletionSampleAExpected.setContent(loadFile("testSampleWithUnknownProject_deletionSampleAExpected.json"));
        assertExpectedEvent(events.get(2), deletionSampleAExpected);
    }

    @Test
    public void testSampleWithUnknownSpace()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create sample A in project /A/A
        // - delete sample A
        // - move project /A/A to space B (it becomes /B/A)
        // - delete space A (project /B/A relation history with space A loses space_id and will be stored as UNKNOWN)
        // - delete project /B/A

        PersonPE deleterSampleA = new PersonPE();
        deleterSampleA.setUserId("deleter_sample_A");

        PersonPE deleterSpaceA = new PersonPE();
        deleterSpaceA.setUserId("deleter_space_A");

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        EventPE deletionSampleA = new EventPE();
        deletionSampleA.setId(1L);
        deletionSampleA.setEventType(EventType.DELETION);
        deletionSampleA.setEntityType(EntityType.SAMPLE);
        deletionSampleA.setIdentifiers(Collections.singletonList("20210420105858331-205249"));
        deletionSampleA.setDescription("20210420105858331-205249");
        deletionSampleA.setReason("Reason Sample A");
        deletionSampleA.setContent(loadFile("testSampleWithUnknownSpace_deletionSampleA.json"));
        deletionSampleA.setRegistrator(deleterSampleA);
        deletionSampleA.setRegistrationDate(dateTimeMillis("2021-04-20 10:59:14.843"));

        EventPE deletionSpaceA = new EventPE();
        deletionSpaceA.setId(2L);
        deletionSpaceA.setEventType(EventType.DELETION);
        deletionSpaceA.setEntityType(EntityType.SPACE);
        deletionSpaceA.setIdentifiers(Collections.singletonList("SPACE_A"));
        deletionSpaceA.setDescription("Description Space A");
        deletionSpaceA.setReason("Reason Space A");
        deletionSpaceA.setRegistrator(deleterSpaceA);
        deletionSpaceA.setRegistrationDate(dateTimeMillis("2021-04-20 10:59:49.513"));

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(3L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210420105829314-205247"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testSampleWithUnknownSpace_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-04-20 11:01:14.957"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-04-20 10:58:14.693"));

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadSpaces(spaceB);
            expectLoadProjects();
            expectLoadExperiments();
            expectLoadLastTimestampsEmpty(EventType.values());
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, null, deletionSpaceA);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionSpaceA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, null, deletionProjectA);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, null);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, null, deletionSampleA);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleA.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet
                    .of(EntityType.DATASET, EntityType.MATERIAL,
                            EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                            EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                expectLoadEvents(EventType.DELETION, entityType, null);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionSpaceAExpected = createExpectedEvent(deletionSpaceA);
        deletionSpaceAExpected.setEntitySpace("SPACE_A");
        deletionSpaceAExpected.setIdentifier("SPACE_A");
        assertExpectedEvent(events.get(0), deletionSpaceAExpected);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_B");
        deletionProjectAExpected.setEntitySpacePermId("200");
        deletionProjectAExpected.setEntityProject("/SPACE_B/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210420105829314-205247");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 10:58:29.000"));
        deletionProjectAExpected.setIdentifier("20210420105829314-205247");
        deletionProjectAExpected.setContent(loadFile("testSampleWithUnknownSpace_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(1), deletionProjectAExpected);

        EventsSearchPE deletionSampleAExpected = createExpectedEvent(deletionSampleA);
        deletionSampleAExpected.setEntitySpace("SPACE_A");
        deletionSampleAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionSampleAExpected.setEntityProjectPermId("20210420105829314-205247");
        deletionSampleAExpected.setEntityRegisterer("registerer_sample_A");
        deletionSampleAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 10:58:58.000"));
        deletionSampleAExpected.setIdentifier("20210420105858331-205249");
        deletionSampleAExpected.setContent(loadFile("testSampleWithUnknownSpace_deletionSampleAExpected.json"));
        assertExpectedEvent(events.get(2), deletionSampleAExpected);
    }

    @Test
    public void testDataSets()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create project /B/B
        // - create experiment /A/A/A
        // - create experiment /B/B/B
        // - create sample /A/A in space A
        // - create sample /B/B/B in project /B/B
        // - create sample /B/B/C in experiment /B/B/B
        // - create dataset A in sample /A/A
        // - move dataset A to sample /B/B/B
        // - delete dataset A
        // - create dataset B in experiment /A/A/A
        // - move dataset B to experiment /B/B/B
        // - delete dataset B
        // - create dataset C in sample /A/A
        // - move sample /A/A to space B
        // - move dataset C to experiment /A/A/A
        // - delete sample /B/A
        // - move experiment /A/A/A to project /B/B
        // - move dataset C to experiment /B/B/B
        // - delete experiment /B/B/A
        // - delete dataset C

        PersonPE deleterDataSetA = new PersonPE();
        deleterDataSetA.setUserId("deleter_dataset_A");

        PersonPE deleterDataSetB = new PersonPE();
        deleterDataSetB.setUserId("deleter_dataset_B");

        PersonPE deleterSampleA = new PersonPE();
        deleterSampleA.setUserId("deleter_sample_A");

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        PersonPE deleterDataSetC = new PersonPE();
        deleterDataSetC.setUserId("deleter_dataset_C");

        EventPE deletionDataSetA = new EventPE();
        deletionDataSetA.setId(1L);
        deletionDataSetA.setEventType(EventType.DELETION);
        deletionDataSetA.setEntityType(EntityType.DATASET);
        deletionDataSetA.setIdentifiers(Collections.singletonList("20210407141933487-205216"));
        deletionDataSetA.setDescription("/STANDARD/1/6633CE8F-DD6E-450B-815B-7910CCF91726/e2/62/cb/20210407141933487-205216");
        deletionDataSetA.setReason("Reason DataSet A");
        deletionDataSetA.setContent(loadFile("testDataSets_deletionDataSetA.json"));
        deletionDataSetA.setRegistrator(deleterDataSetA);
        deletionDataSetA.setRegistrationDate(dateTimeMillis("2021-04-07 15:22:50.799"));

        EventPE deletionDataSetB = new EventPE();
        deletionDataSetB.setId(2L);
        deletionDataSetB.setEventType(EventType.DELETION);
        deletionDataSetB.setEntityType(EntityType.DATASET);
        deletionDataSetB.setIdentifiers(Collections.singletonList("20210407152453960-205217"));
        deletionDataSetB.setDescription("/STANDARD/1/6633CE8F-DD6E-450B-815B-7910CCF91726/ac/06/b7/20210407152453960-205217");
        deletionDataSetB.setReason("Reason DataSet B");
        deletionDataSetB.setContent(loadFile("testDataSets_deletionDataSetB.json"));
        deletionDataSetB.setRegistrator(deleterDataSetB);
        deletionDataSetB.setRegistrationDate(dateTimeMillis("2021-04-07 15:26:02.193"));

        EventPE deletionSampleA = new EventPE();
        deletionSampleA.setId(3L);
        deletionSampleA.setEventType(EventType.DELETION);
        deletionSampleA.setEntityType(EntityType.SAMPLE);
        deletionSampleA.setIdentifiers(Collections.singletonList("20210407141248879-205213"));
        deletionSampleA.setDescription("20210407141248879-205213");
        deletionSampleA.setReason("Reason Sample A");
        deletionSampleA.setContent(loadFile("testDataSets_deletionSampleA.json"));
        deletionSampleA.setRegistrator(deleterSampleA);
        deletionSampleA.setRegistrationDate(dateTimeMillis("2021-04-07 15:28:21.507"));

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(4L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210407141206022-205211"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testDataSets_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-04-07 15:29:56.346"));

        EventPE deletionDataSetC = new EventPE();
        deletionDataSetC.setId(5L);
        deletionDataSetC.setEventType(EventType.DELETION);
        deletionDataSetC.setEntityType(EntityType.DATASET);
        deletionDataSetC.setIdentifiers(Collections.singletonList("20210407152643902-205218"));
        deletionDataSetC.setDescription("/STANDARD/1/6633CE8F-DD6E-450B-815B-7910CCF91726/2e/9c/b2/20210407152643902-205218");
        deletionDataSetC.setReason("Reason DataSet C");
        deletionDataSetC.setContent(loadFile("testDataSets_deletionDataSetC.json"));
        deletionDataSetC.setRegistrator(deleterDataSetC);
        deletionDataSetC.setRegistrationDate(dateTimeMillis("2021-04-07 15:30:16.793"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("SPACE_A");
        spaceA.setRegistrationDate(dateTimeMillis("2021-03-29 15:07:55.947"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-03-29 15:08:03.609"));

        Space spaceAv3 = new Space();
        spaceAv3.setCode("SPACE_A");

        Space spaceBv3 = new Space();
        spaceBv3.setCode("SPACE_B");

        ProjectFetchOptions projectFo = new ProjectFetchOptions();
        projectFo.withSpace();
        projectFo.withHistory();

        Project projectA = new Project();
        projectA.setCode("PROJECT_A");
        projectA.setPermId(new ProjectPermId("20210329151103947-205180"));
        projectA.setSpace(spaceAv3);
        projectA.setRegistrationDate(dateTimeMillis("2021-03-29 15:11:03.947"));
        projectA.setHistory(Collections.emptyList());
        projectA.setFetchOptions(projectFo);

        Project projectB = new Project();
        projectB.setCode("PROJECT_B");
        projectB.setPermId(new ProjectPermId("20210401151815315-205197"));
        projectB.setSpace(spaceBv3);
        projectB.setRegistrationDate(dateTimeMillis("2021-04-01 15:18:15.315"));
        projectB.setHistory(Collections.emptyList());
        projectB.setFetchOptions(projectFo);

        ExperimentFetchOptions experimentFo = new ExperimentFetchOptions();
        experimentFo.withProject();
        experimentFo.withHistory();

        Experiment experimentB = new Experiment();
        experimentB.setCode("EXPERIMENT_B");
        experimentB.setPermId(new ExperimentPermId("20210407141217738-205212"));
        experimentB.setProject(projectB);
        experimentB.setRegistrationDate(dateTimeMillis("2021-04-07 14:12:17.738"));
        experimentB.setHistory(Collections.emptyList());
        experimentB.setFetchOptions(experimentFo);

        SampleFetchOptions sampleFo = new SampleFetchOptions();
        sampleFo.withSpace();
        sampleFo.withProject();
        sampleFo.withExperiment();
        sampleFo.withHistory();

        Sample sampleB = new Sample();
        sampleB.setCode("SAMPLE_B");
        sampleB.setPermId(new SamplePermId("20210407141318103-205214"));
        sampleB.setProject(projectB);
        sampleB.setRegistrationDate(dateTimeMillis("2021-04-07 14:13:18.103"));
        sampleB.setHistory(Collections.emptyList());
        sampleB.setFetchOptions(sampleFo);

        Sample sampleC = new Sample();
        sampleC.setCode("SAMPLE_C");
        sampleC.setPermId(new SamplePermId("20210407141344363-205215"));
        sampleC.setExperiment(experimentB);
        sampleC.setRegistrationDate(dateTimeMillis("2021-04-07 14:13:44.363"));
        sampleC.setHistory(Collections.emptyList());
        sampleC.setFetchOptions(sampleFo);

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadLastTimestampsEmpty(EventType.MOVEMENT, EventType.FREEZING);
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-04-07 00:30:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-04-07 00:29:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-04-07 00:28:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-04-07 00:27:00.000"));
            expectLoadLastTimestamp(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-04-07 00:26:00.000"));

            expectLoadEvents(EventType.DELETION, EntityType.SPACE, dateTimeMillis("2021-04-07 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, dateTimeMillis("2021-04-07 00:26:00.000"));
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, dateTimeMillis("2021-04-07 00:26:00.000"), deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, dateTimeMillis("2021-04-07 00:26:00.000"), deletionSampleA);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, dateTimeMillis("2021-04-07 00:26:00.000"), deletionDataSetA);
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, deletionDataSetA.getRegistrationDateInternal(), deletionDataSetB,
                    deletionDataSetC);
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, deletionDataSetC.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL, EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                Date randomTimestamp = new Date((long) (Math.random() * 10000));
                expectLoadLastTimestamp(EventType.DELETION, entityType, randomTimestamp);
                expectLoadEvents(EventType.DELETION, entityType, randomTimestamp);
            }

            expectLoadSpaces(spaceA, spaceB);
            expectLoadProjects(projectA, projectB);
            expectLoadExperiments(experimentB);
            expectLoadSamples(sampleB, sampleC);

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 5);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_B");
        deletionExperimentAExpected.setEntitySpacePermId("200");
        deletionExperimentAExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionExperimentAExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-07 14:12:06.000"));
        deletionExperimentAExpected.setIdentifier("20210407141206022-205211");
        deletionExperimentAExpected.setContent(loadFile("testDataSets_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(0), deletionExperimentAExpected);

        EventsSearchPE deletionSampleAExpected = createExpectedEvent(deletionSampleA);
        deletionSampleAExpected.setEntitySpace("SPACE_B");
        deletionSampleAExpected.setEntitySpacePermId("200");
        deletionSampleAExpected.setEntityRegisterer("registerer_sample_A");
        deletionSampleAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-07 14:12:48.000"));
        deletionSampleAExpected.setIdentifier("20210407141248879-205213");
        deletionSampleAExpected.setContent(loadFile("testDataSets_deletionSampleAExpected.json"));
        assertExpectedEvent(events.get(1), deletionSampleAExpected);

        EventsSearchPE deletionDataSetAExpected = createExpectedEvent(deletionDataSetA);
        deletionDataSetAExpected.setEntitySpace("SPACE_B");
        deletionDataSetAExpected.setEntitySpacePermId("200");
        deletionDataSetAExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionDataSetAExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionDataSetAExpected.setEntityRegisterer("registerer_dataset_A");
        deletionDataSetAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-07 14:19:33.000"));
        deletionDataSetAExpected.setIdentifier("20210407141933487-205216");
        deletionDataSetAExpected.setContent(loadFile("testDataSets_deletionDataSetAExpected.json"));
        assertExpectedEvent(events.get(2), deletionDataSetAExpected);

        EventsSearchPE deletionDataSetBExpected = createExpectedEvent(deletionDataSetB);
        deletionDataSetBExpected.setEntitySpace("SPACE_B");
        deletionDataSetBExpected.setEntitySpacePermId("200");
        deletionDataSetBExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionDataSetBExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionDataSetBExpected.setEntityRegisterer("registerer_dataset_B");
        deletionDataSetBExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-07 15:24:54.000"));
        deletionDataSetBExpected.setIdentifier("20210407152453960-205217");
        deletionDataSetBExpected.setContent(loadFile("testDataSets_deletionDataSetBExpected.json"));
        assertExpectedEvent(events.get(3), deletionDataSetBExpected);

        EventsSearchPE deletionDataSetCExpected = createExpectedEvent(deletionDataSetC);
        deletionDataSetCExpected.setEntitySpace("SPACE_B");
        deletionDataSetCExpected.setEntitySpacePermId("200");
        deletionDataSetCExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionDataSetCExpected.setEntityProjectPermId("20210401151815315-205197");
        deletionDataSetCExpected.setEntityRegisterer("registerer_dataset_C");
        deletionDataSetCExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-07 15:26:44.000"));
        deletionDataSetCExpected.setIdentifier("20210407152643902-205218");
        deletionDataSetCExpected.setContent(loadFile("testDataSets_deletionDataSetCExpected.json"));
        assertExpectedEvent(events.get(4), deletionDataSetCExpected);
    }

    @Test
    public void testDataSetWithUnknownProject()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create project /A/A
        // - create project /B/B
        // - create experiment /A/A/A
        // - create dataset A in experiment /A/A/A
        // - delete dataset A
        // - move experiment /A/A/A to project /B/B (it becomes /B/B/A)
        // - delete project /A/A (experiment /B/B/A relation history with project /A/A loses proj_id and will be stored as UNKNOWN)
        // - delete experiment /B/B/A

        PersonPE deleterDataSetA = new PersonPE();
        deleterDataSetA.setUserId("deleter_dataset_A");

        PersonPE deleterProjectA = new PersonPE();
        deleterProjectA.setUserId("deleter_project_A");

        PersonPE deleterExperimentA = new PersonPE();
        deleterExperimentA.setUserId("deleter_experiment_A");

        EventPE deletionDataSetA = new EventPE();
        deletionDataSetA.setId(1L);
        deletionDataSetA.setEventType(EventType.DELETION);
        deletionDataSetA.setEntityType(EntityType.DATASET);
        deletionDataSetA.setIdentifiers(Collections.singletonList("20210420114435382-205253"));
        deletionDataSetA.setDescription("/STANDARD/1/6633CE8F-DD6E-450B-815B-7910CCF91726/0e/c5/9e/20210420114435382-205253");
        deletionDataSetA.setReason("Reason DataSet A");
        deletionDataSetA.setContent(loadFile("testDataSetWithUnknownProject_deletionDataSetA.json"));
        deletionDataSetA.setRegistrator(deleterDataSetA);
        deletionDataSetA.setRegistrationDate(dateTimeMillis("2021-04-20 11:45:56.375"));

        EventPE deletionProjectA = new EventPE();
        deletionProjectA.setId(2L);
        deletionProjectA.setEventType(EventType.DELETION);
        deletionProjectA.setEntityType(EntityType.PROJECT);
        deletionProjectA.setIdentifiers(Collections.singletonList("20210420114205083-205250"));
        deletionProjectA.setDescription("Description Project A");
        deletionProjectA.setReason("Reason Project A");
        deletionProjectA.setContent(loadFile("testDataSetWithUnknownProject_deletionProjectA.json"));
        deletionProjectA.setRegistrator(deleterProjectA);
        deletionProjectA.setRegistrationDate(dateTimeMillis("2021-04-20 11:46:29.082"));

        EventPE deletionExperimentA = new EventPE();
        deletionExperimentA.setId(3L);
        deletionExperimentA.setEventType(EventType.DELETION);
        deletionExperimentA.setEntityType(EntityType.EXPERIMENT);
        deletionExperimentA.setIdentifiers(Collections.singletonList("20210420114236830-205252"));
        deletionExperimentA.setDescription("Description Experiment A");
        deletionExperimentA.setReason("Reason Experiment A");
        deletionExperimentA.setContent(loadFile("testDataSetWithUnknownProject_deletionExperimentA.json"));
        deletionExperimentA.setRegistrator(deleterExperimentA);
        deletionExperimentA.setRegistrationDate(dateTimeMillis("2021-04-20 11:46:58.116"));

        SpacePE spaceA = new SpacePE();
        spaceA.setId(100L);
        spaceA.setCode("SPACE_A");
        spaceA.setRegistrationDate(dateTimeMillis("2021-04-20 11:41:46.947"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-04-20 11:41:51.801"));

        Space spaceBv3 = new Space();
        spaceBv3.setCode("SPACE_B");

        ProjectFetchOptions projectFo = new ProjectFetchOptions();
        projectFo.withSpace();
        projectFo.withHistory();

        Project projectB = new Project();
        projectB.setCode("PROJECT_B");
        projectB.setPermId(new ProjectPermId("20210420114213918-205251"));
        projectB.setSpace(spaceBv3);
        projectB.setRegistrationDate(dateTimeMillis("2021-04-20 11:42:13.918"));
        projectB.setHistory(Collections.emptyList());
        projectB.setFetchOptions(projectFo);

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadSpaces(spaceA, spaceB);
            expectLoadProjects(projectB);
            expectLoadExperiments();
            expectLoadSamples();
            expectLoadLastTimestampsEmpty(EventType.values());
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, null);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, null, deletionProjectA);
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, deletionProjectA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, null, deletionExperimentA);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, deletionExperimentA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, null);
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, null, deletionDataSetA);
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, deletionDataSetA.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet
                    .of(EntityType.MATERIAL,
                            EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                            EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                expectLoadEvents(EventType.DELETION, entityType, null);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionProjectAExpected = createExpectedEvent(deletionProjectA);
        deletionProjectAExpected.setEntitySpace("SPACE_A");
        deletionProjectAExpected.setEntitySpacePermId("100");
        deletionProjectAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionProjectAExpected.setEntityProjectPermId("20210420114205083-205250");
        deletionProjectAExpected.setEntityRegisterer("registerer_project_A");
        deletionProjectAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 11:42:05.000"));
        deletionProjectAExpected.setIdentifier("20210420114205083-205250");
        deletionProjectAExpected.setContent(loadFile("testDataSetWithUnknownProject_deletionProjectAExpected.json"));
        assertExpectedEvent(events.get(0), deletionProjectAExpected);

        EventsSearchPE deletionExperimentAExpected = createExpectedEvent(deletionExperimentA);
        deletionExperimentAExpected.setEntitySpace("SPACE_B");
        deletionExperimentAExpected.setEntitySpacePermId("200");
        deletionExperimentAExpected.setEntityProject("/SPACE_B/PROJECT_B");
        deletionExperimentAExpected.setEntityProjectPermId("20210420114213918-205251");
        deletionExperimentAExpected.setEntityRegisterer("registerer_experiment_A");
        deletionExperimentAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 11:42:36.000"));
        deletionExperimentAExpected.setIdentifier("20210420114236830-205252");
        deletionExperimentAExpected.setContent(loadFile("testDataSetWithUnknownProject_deletionExperimentAExpected.json"));
        assertExpectedEvent(events.get(1), deletionExperimentAExpected);

        EventsSearchPE deletionDataSetAExpected = createExpectedEvent(deletionDataSetA);
        deletionDataSetAExpected.setEntitySpace("SPACE_A");
        deletionDataSetAExpected.setEntitySpacePermId("100");
        deletionDataSetAExpected.setEntityProject("/SPACE_A/PROJECT_A");
        deletionDataSetAExpected.setEntityProjectPermId("20210420114205083-205250");
        deletionDataSetAExpected.setEntityRegisterer("registerer_dataset_A");
        deletionDataSetAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 11:44:35.000"));
        deletionDataSetAExpected.setIdentifier("20210420114435382-205253");
        deletionDataSetAExpected.setContent(loadFile("testDataSetWithUnknownProject_deletionDataSetAExpected.json"));
        assertExpectedEvent(events.get(2), deletionDataSetAExpected);
    }

    @Test
    public void testDataSetWithUnknownSpace()
    {
        // Tests the following scenario:
        // - create space A
        // - create space B
        // - create sample A in space A
        // - create dataset A in sample A
        // - delete dataset A
        // - move sample A to space B
        // - delete space A (sample A relation history with space A loses space_id and will be stored as UNKNOWN)
        // - delete sample A

        PersonPE deleterDataSetA = new PersonPE();
        deleterDataSetA.setUserId("deleter_dataset_A");

        PersonPE deleterSpaceA = new PersonPE();
        deleterSpaceA.setUserId("deleter_space_A");

        PersonPE deleterSampleA = new PersonPE();
        deleterSampleA.setUserId("deleter_sample_A");

        EventPE deletionDataSetA = new EventPE();
        deletionDataSetA.setId(1L);
        deletionDataSetA.setEventType(EventType.DELETION);
        deletionDataSetA.setEntityType(EntityType.DATASET);
        deletionDataSetA.setIdentifiers(Collections.singletonList("20210420131858024-205259"));
        deletionDataSetA.setDescription("/STANDARD/1/6633CE8F-DD6E-450B-815B-7910CCF91726/a8/b5/68/20210420131858024-205259");
        deletionDataSetA.setReason("Reason DataSet A");
        deletionDataSetA.setContent(loadFile("testDataSetWithUnknownSpace_deletionDataSetA.json"));
        deletionDataSetA.setRegistrator(deleterDataSetA);
        deletionDataSetA.setRegistrationDate(dateTimeMillis("2021-04-20 13:19:23.605"));

        EventPE deletionSpaceA = new EventPE();
        deletionSpaceA.setId(2L);
        deletionSpaceA.setEventType(EventType.DELETION);
        deletionSpaceA.setEntityType(EntityType.SPACE);
        deletionSpaceA.setIdentifiers(Collections.singletonList("SPACE_A"));
        deletionSpaceA.setDescription("Description Space A");
        deletionSpaceA.setReason("Reason Space A");
        deletionSpaceA.setRegistrator(deleterSpaceA);
        deletionSpaceA.setRegistrationDate(dateTimeMillis("2021-04-20 13:19:51.671"));

        EventPE deletionSampleA = new EventPE();
        deletionSampleA.setId(3L);
        deletionSampleA.setEventType(EventType.DELETION);
        deletionSampleA.setEntityType(EntityType.SAMPLE);
        deletionSampleA.setIdentifiers(Collections.singletonList("20210420131737031-205258"));
        deletionSampleA.setDescription("20210420131737031-205258");
        deletionSampleA.setReason("Reason Sample A");
        deletionSampleA.setContent(loadFile("testDataSetWithUnknownSpace_deletionSampleA.json"));
        deletionSampleA.setRegistrator(deleterSampleA);
        deletionSampleA.setRegistrationDate(dateTimeMillis("2021-04-20 13:20:10.750"));

        SpacePE spaceB = new SpacePE();
        spaceB.setId(200L);
        spaceB.setCode("SPACE_B");
        spaceB.setRegistrationDate(dateTimeMillis("2021-04-20 11:41:51.801"));

        List<EventsSearchPE> events = new ArrayList<>();
        {
            expectLoadSpaces(spaceB);
            expectLoadProjects();
            expectLoadExperiments();
            expectLoadSamples();
            expectLoadLastTimestampsEmpty(EventType.values());
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, null, deletionSpaceA);
            expectLoadEvents(EventType.DELETION, EntityType.SPACE, deletionSpaceA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.PROJECT, null);
            expectLoadEvents(EventType.DELETION, EntityType.EXPERIMENT, null);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, null, deletionSampleA);
            expectLoadEvents(EventType.DELETION, EntityType.SAMPLE, deletionSampleA.getRegistrationDateInternal());
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, null, deletionDataSetA);
            expectLoadEvents(EventType.DELETION, EntityType.DATASET, deletionDataSetA.getRegistrationDateInternal());

            for (EntityType entityType : EnumSet.of(EntityType.MATERIAL,
                    EntityType.ATTACHMENT, EntityType.PROPERTY_TYPE,
                    EntityType.VOCABULARY, EntityType.AUTHORIZATION_GROUP, EntityType.METAPROJECT))
            {
                expectLoadEvents(EventType.DELETION, entityType, null);
            }

            expectCreateEvents(events);
        }

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        assertEquals(events.size(), 3);

        EventsSearchPE deletionSpaceAExpected = createExpectedEvent(deletionSpaceA);
        deletionSpaceAExpected.setEntitySpace("SPACE_A");
        deletionSpaceAExpected.setIdentifier("SPACE_A");
        assertExpectedEvent(events.get(0), deletionSpaceAExpected);

        EventsSearchPE deletionSampleAExpected = createExpectedEvent(deletionSampleA);
        deletionSampleAExpected.setEntitySpace("SPACE_B");
        deletionSampleAExpected.setEntitySpacePermId("200");
        deletionSampleAExpected.setEntityRegisterer("registerer_sample_A");
        deletionSampleAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 13:17:37.000"));
        deletionSampleAExpected.setIdentifier("20210420131737031-205258");
        deletionSampleAExpected.setContent(loadFile("testDataSetWithUnknownSpace_deletionSampleAExpected.json"));
        assertExpectedEvent(events.get(1), deletionSampleAExpected);

        EventsSearchPE deletionDataSetAExpected = createExpectedEvent(deletionDataSetA);
        deletionDataSetAExpected.setEntitySpace("SPACE_A");
        deletionDataSetAExpected.setEntityRegisterer("registerer_dataset_A");
        deletionDataSetAExpected.setEntityRegistrationTimestamp(dateTimeMillis("2021-04-20 13:18:58.000"));
        deletionDataSetAExpected.setIdentifier("20210420131858024-205259");
        deletionDataSetAExpected.setContent(loadFile("testDataSetWithUnknownSpace_deletionDataSetAExpected.json"));
        assertExpectedEvent(events.get(2), deletionDataSetAExpected);
    }

    private void expectLoadSpaces(SpacePE... spaces)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).loadSpaces(with(any(List.class)));
                will(returnValue(Arrays.asList(spaces)));
            }
        });
    }

    private void expectLoadProjects(Project... projects)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).loadProjects(with(any(List.class)), with(any(ProjectFetchOptions.class)));
                will(returnValue(Arrays.asList(projects)));
            }
        });
    }

    private void expectLoadExperiments(Experiment... experiments)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).loadExperiments(with(any(List.class)), with(any(ExperimentFetchOptions.class)));
                will(returnValue(Arrays.asList(experiments)));
            }
        });
    }

    private void expectLoadSamples(Sample... samples)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).loadSamples(with(any(List.class)), with(any(SampleFetchOptions.class)));
                will(returnValue(Arrays.asList(samples)));
            }
        });
    }

    private void expectLoadLastTimestampsEmpty(EventType... eventTypes)
    {
        mockery.checking(new Expectations()
        {
            {
                for (EventType eventType : eventTypes)
                {
                    allowing(dataSource).loadLastEventsSearchTimestamp(with(eventType), with(any(EntityType.class)));
                    will(returnValue(null));
                }
            }
        });
    }

    private void expectLoadLastTimestamp(EventType eventType, EntityType entityType, Date lastSeenTimestamp)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).loadLastEventsSearchTimestamp(with(eventType), with(entityType));
                will(returnValue(lastSeenTimestamp));
            }
        });
    }

    private void expectLoadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestamp, EventPE... events)
    {
        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(with(eventType), with(entityType), with(lastSeenTimestamp));
                will(returnValue(Arrays.asList(events)));
            }
        });
    }

    private void expectCreateEvents(final List<EventsSearchPE> events)
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(dataSource).createEventsSearch(with(any(EventsSearchPE.class)));
                will(new CustomAction("collect events")
                {
                    @Override public Object invoke(Invocation invocation) throws Throwable
                    {
                        events.add((EventsSearchPE) invocation.getParameter(0));
                        return null;
                    }
                });
            }
        });
    }

    private EventsSearchPE createExpectedEvent(EventPE originalEvent)
    {
        EventsSearchPE expectedEvent = new EventsSearchPE();
        expectedEvent.setEventType(originalEvent.getEventType());
        expectedEvent.setEntityType(originalEvent.getEntityType());
        expectedEvent.setDescription(originalEvent.getDescription());
        expectedEvent.setReason(originalEvent.getReason());
        if (originalEvent.getAttachmentContent() != null)
        {
            expectedEvent.setAttachmentContent(originalEvent.getAttachmentContent().getId());
        }
        expectedEvent.setRegisterer(originalEvent.getRegistrator());
        expectedEvent.setRegistrationTimestamp(originalEvent.getRegistrationDate());
        return expectedEvent;
    }

    private void assertExpectedEvent(EventsSearchPE actualEvent, EventsSearchPE expectedEvent)
    {
        try
        {
            // make sure in both events the registerer display settings are initialized
            actualEvent.getRegisterer().getDisplaySettings();
            expectedEvent.getRegisterer().getDisplaySettings();

            // make sure in both events the content is formatted the same way
            if (actualEvent.getContent() != null)
            {
                Object actualContent = objectMapper.readValue(actualEvent.getContent(), Object.class);
                actualEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualContent));
            }
            if (expectedEvent.getContent() != null)
            {
                Object expectedContent = objectMapper.readValue(expectedEvent.getContent(), Object.class);
                expectedEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedContent));
            }

            // do the comparison
            String actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualEvent);
            String expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedEvent);
            assertEquals(actualJson, expectedJson);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String loadFile(String fileName)
    {
        return FileUtilities.loadToString(resources.getResourceFile(fileName));
    }

    private static Date dateTimeMillis(String dateTimeMillis)
    {
        try
        {
            return DATE_TIME_MILLIS_FORMAT.parse(dateTimeMillis);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
