package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

public class TestUtils
{

    public static final String XLS_PARAM = "xls";

    public static final String SCRIPTS_PARAM = "scripts";

    public static final String XLS_IMPORT_API = "xls-import-api";

    static Vocabulary getVocabulary(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        criteria.withId().thatEquals(new VocabularyPermId(code));

        VocabularyFetchOptions fo = new VocabularyFetchOptions();
        fo.withTerms();

        SearchResult<Vocabulary> result = v3api.searchVocabularies(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static List<Vocabulary> getAllVocabularies(IApplicationServerInternalApi v3api, String sessionToken)
    {
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        VocabularyFetchOptions fo = new VocabularyFetchOptions();
        fo.withTerms();

        SearchResult<Vocabulary> result = v3api.searchVocabularies(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects();
        } else
        {
            return null;
        }
    }

    static SampleType getSampleType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<SampleType> result = v3api.searchSampleTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static ExperimentType getExperimentType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        ExperimentTypeSearchCriteria criteria = new ExperimentTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<ExperimentType> result = v3api.searchExperimentTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static DataSetType getDatasetType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<DataSetType> result = v3api.searchDataSetTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static PropertyType getPropertyType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        PropertyTypeFetchOptions fo = new PropertyTypeFetchOptions();
        fo.withVocabulary();

        SearchResult<PropertyType> result = v3api.searchPropertyTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Space getSpace(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEquals(code);

        SpaceFetchOptions fo = new SpaceFetchOptions();

        SearchResult<Space> result = v3api.searchSpaces(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Project getProject(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEquals(code);

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();

        SearchResult<Project> result = v3api.searchProjects(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Experiment getExperiment(IApplicationServerInternalApi v3api, String sessionToken, String experimentCode, String projectCode,
            String spaceCode)
    {
        List<IExperimentId> ids = new ArrayList<>();
        ids.add(new ExperimentIdentifier(spaceCode, projectCode, experimentCode));

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProject();
        fo.withProperties();
        fo.withType();

        List<Experiment> result = v3api.getExperiments(sessionToken, ids, fo).values().stream().collect(Collectors.toList());

        if (result.size() > 0)
        {
            return result.get(0);
        } else
        {
            return null;
        }
    }

    static Sample getSample(IApplicationServerInternalApi v3api, String sessionToken, String sampleCode, String spaceCode)
    {
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier(spaceCode, null, null, sampleCode));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withChildren();
        fo.withParents();
        fo.withExperiment();
        fo.withProject();
        fo.withProperties();
        fo.withSpace();
        fo.withType();

        List<Sample> result = v3api.getSamples(sessionToken, ids, fo).values().stream().collect(Collectors.toList());

        if (result.size() > 0)
        {
            return result.get(0);
        } else
        {
            return null;
        }
    }

    static String createFrom(IApplicationServerInternalApi v3api, String sessionToken, Path... xls_paths) throws IOException
    {
        List<byte[]> excels = new ArrayList<>();
        for (Path xls_path : xls_paths)
        {
            byte[] xls = Files.readAllBytes(xls_path);
            excels.add(xls);
        }
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter(XLS_PARAM, excels);
        return (String) v3api.executeCustomASService(sessionToken, new CustomASServiceCode(XLS_IMPORT_API), options);
    }

    static String createFrom(IApplicationServerInternalApi v3api, String sessionToken, Map<String, String> scripts, Path... xls_paths)
            throws IOException
    {
        List<byte[]> excels = new ArrayList<>();
        for (Path xls_path : xls_paths)
        {
            byte[] xls = Files.readAllBytes(xls_path);
            excels.add(xls);
        }
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter(XLS_PARAM, excels);
        options.withParameter(SCRIPTS_PARAM, scripts);
        return (String) v3api.executeCustomASService(sessionToken, new CustomASServiceCode(XLS_IMPORT_API), options);
    }

    static String getValidationScript()
    {
        return "def validate(entity, isNew):\n  if isNew:\n    return";
    }

    static String getDynamicScript()
    {
        return "def calculate():\n    return 1";
    }

    static Map<String, String> getValidationPluginMap()
    {
        String dynamicScriptString = getValidationScript();
        Map<String, String> scriptsMap = new HashMap<>();
        scriptsMap.put("valid.py", dynamicScriptString);

        return scriptsMap;
    }

    static Map<String, String> getDynamicPluginMap()
    {
        String dynamicScriptString = getDynamicScript();
        Map<String, String> scriptsMap = new HashMap<>();
        scriptsMap.put("dynamic/dynamic.py", dynamicScriptString);

        return scriptsMap;
    }

}
