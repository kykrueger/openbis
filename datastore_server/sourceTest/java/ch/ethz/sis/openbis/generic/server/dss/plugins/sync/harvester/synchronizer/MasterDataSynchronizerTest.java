package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.spi.NOPLogger;
import org.apache.log4j.spi.NOPLoggerRepository;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

public class MasterDataSynchronizerTest
{
    private static final String USER_ID = "user";

    private static final String PASSWORD = "password";

    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private ISynchronizerFacade facade;

    private ICommonServer server;

    private IApplicationServerApi v3api;

    private MasterDataSynchronizer synchronizer;

    private Monitor monitor;

    private SyncConfig config;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        facade = context.mock(ISynchronizerFacade.class);
        server = context.mock(ICommonServer.class);
        v3api = context.mock(IApplicationServerApi.class);
        context.checking(new Expectations()
            {
                {
                    allowing(server).tryAuthenticate(USER_ID, PASSWORD);
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    allowing(server).listFileFormatTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList()));
                }
            });
        config = new SyncConfig();
        config.setHarvesterUser(USER_ID);
        config.setHarvesterPass(PASSWORD);
        synchronizer = new MasterDataSynchronizer(config, facade, server, v3api);
        monitor = new Monitor("test", new NOPLogger(new NOPLoggerRepository(), "null"));
    }

    @Test
    // TODO finish test
    public void testSystemManagedVocabularyExist()
    {
        // Given
        config.setMasterDataUpdateAllowed(true);
        TestFixtureBuilder builder = new TestFixtureBuilder();
        NewVocabulary v1 = new VocabularyBuilder("$A").managedInternally().system()
                /*.description("1. description")*/.get();
        builder.existingVocabularies(v1);
        builder.incomingVocabularies(new VocabularyBuilder("A").managedInternally().system().get());
        MasterData masterData = builder.prepare();
        prepareAddVocabularyTerms(v1.getCode(), v1.getId());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    private final class TestFixtureBuilder
    {
        private List<FileFormatType> existingFileFormatTypes = Collections.emptyList();

        private List<FileFormatType> incomingFileFormatTypes = Collections.emptyList();

        private List<Script> existingScripts = Collections.emptyList();

        private List<Script> incomingScripts = Collections.emptyList();

        private List<NewVocabulary> existingVocabularies = Collections.emptyList();

        private List<NewVocabulary> incomingVocabularies = Collections.emptyList();

        private List<PropertyType> existingPropertyTypes = Collections.emptyList();

        private List<PropertyType> incomingPropertyTypes = Collections.emptyList();

        private List<MaterialType> existingMaterialTypes = Collections.emptyList();

        private List<MaterialType> incomingMaterialTypes = Collections.emptyList();

        private List<ExternalDms> existingExternalDmss = Collections.emptyList();

        private List<ExternalDms> incomingExternalDmss = Collections.emptyList();

        TestFixtureBuilder existingVocabularies(NewVocabulary... vocabularies)
        {
            existingVocabularies = Arrays.asList(vocabularies);
            return this;
        }

        TestFixtureBuilder incomingVocabularies(NewVocabulary... vocabularies)
        {
            incomingVocabularies = Arrays.asList(vocabularies);
            return this;
        }

        MasterData prepare()
        {
            List<ExternalDmsPermId> existingExternalDmssIds = existingExternalDmss.stream().map(ExternalDms::getPermId).collect(Collectors.toList());
            Map<ExternalDmsPermId, ExternalDms> existingExternalDmssMap = existingExternalDmss.stream().collect(
                    Collectors.toMap(ExternalDms::getPermId, Function.identity()));
            context.checking(new Expectations()
                {
                    {
                        allowing(server).listFileFormatTypes(SESSION_TOKEN);
                        will(returnValue(existingFileFormatTypes));
                        allowing(server).listScripts(SESSION_TOKEN, null, null);
                        will(returnValue(existingScripts));
                        allowing(server).listVocabularies(SESSION_TOKEN, true, false);
                        will(returnValue(existingVocabularies));
                        allowing(server).listPropertyTypes(SESSION_TOKEN, false);
                        will(returnValue(existingPropertyTypes));
                        allowing(server).listMaterialTypes(SESSION_TOKEN);
                        will(returnValue(existingMaterialTypes));
                        allowing(v3api).getExternalDataManagementSystems(with(SESSION_TOKEN), with(existingExternalDmssIds),
                                with(any(ExternalDmsFetchOptions.class)));
                        will(returnValue(existingExternalDmssMap));
                        one(facade).printSummary();
                    }
                });
            MasterData masterData = new MasterData();
            masterData.setVocabulariesToProcess(incomingVocabularies.stream().collect(
                    Collectors.toMap(NewVocabulary::getCode, Function.identity())));
            return masterData;
        }
    }

    private void prepareAddVocabularyTerms(String vocabularyCode, long id, VocabularyTerm... vocabularyTerms)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).addVocabularyTerms(vocabularyCode, new TechId(id), Arrays.asList(vocabularyTerms));
                }
            });
    }

    private static final class VocabularyBuilder
    {
        private static long counter;

        private NewVocabulary vocabulary = new NewVocabulary();

        VocabularyBuilder(String code)
        {
            vocabulary.setCode(code);
            vocabulary.setId(counter++);
        }

        NewVocabulary get()
        {
            return vocabulary;
        }

        VocabularyBuilder chosenFromList(boolean chosenFromList)
        {
            vocabulary.setChosenFromList(chosenFromList);
            return this;
        }

        VocabularyBuilder description(String description)
        {
            vocabulary.setDescription(description);
            return this;
        }

        VocabularyBuilder managedInternally()
        {
            vocabulary.setManagedInternally(true);
            return this;
        }

        VocabularyBuilder system()
        {
            return registrator("system");
        }

        VocabularyBuilder registrator(String userId)
        {
            Person person = new Person();
            person.setUserId(userId);
            vocabulary.setRegistrator(person);
            return this;
        }
    }
}
