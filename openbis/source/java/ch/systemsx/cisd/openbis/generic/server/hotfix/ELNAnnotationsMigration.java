package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ELNAnnotationsMigration {

    private static boolean contains(String permId, List<Sample> samples) {
        for (Sample sample:samples) {
            if (sample.getPermId().getPermId().equals(permId)) {
                return true;
            }
        }
        return false;
    }

    public static void beforeUpgrade() throws Exception {
        System.out.println("ELNAnnotationsMigration beforeUpgrade START");
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = api.loginAsSystem();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("$ANNOTATIONS_STATE");
        SampleFetchOptions options = new SampleFetchOptions();
        options.withProperties();
        options.withParents();
        options.withChildren();
        options.sortBy().permId();
        options.from(0);
        options.count(0);

        SearchResult<Sample> results = api.searchSamples(sessionToken, criteria, options);
        int from = 0;
        int count = 10000;
        int total = results.getTotalCount();
        System.out.println("ELNAnnotationsMigration from: " + from + " count: " + count + " total: " + total);

        List<SampleUpdate> sampleUpdates = new ArrayList<>();
        while (total > from) {
            options.from(from);
            options.count(count);
            System.out.println("ELNAnnotationsMigration from: " + from + " count: " + count + " total: " + total);
            for (Sample sample:api.searchSamples(sessionToken, criteria, options).getObjects()) {
                SampleUpdate sampleUpdate = null;
                String annotations = sample.getProperty("$ANNOTATIONS_STATE");
                // XML Properties can potentially contain garbage values, ignore those
                if (!annotations.isEmpty() && !annotations.startsWith("�")) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    ByteArrayInputStream input = new ByteArrayInputStream(annotations.getBytes("UTF-8"));
                    try {
                        Document document = builder.parse(input);
                        Element documentElement = document.getDocumentElement();
                        if (documentElement.hasChildNodes()) {
                            NodeList sampleNodes = documentElement.getChildNodes();
                            for (int sampleIdx = 0; sampleIdx < sampleNodes.getLength(); sampleIdx++) {
                                Node sampleNode = sampleNodes.item(sampleIdx);
                                NamedNodeMap sampleNodeAttributes = sampleNode.getAttributes();
                                // Every sample node has 3 information attributes, this was to paint the interface when using manage properties on the GWT Core UI
                                // To contain annotations the node needs to have more than 3 attributes
                                int sampleNodeAttributesNumber = sampleNodeAttributes.getLength();
                                if (sampleNodeAttributesNumber > 3) {
                                    // permId is always be present for every sample node is one of the 3 information attributes
                                    String samplePermId = sampleNodeAttributes.getNamedItem("permId").getNodeValue();
                                    for (int attributeIdx = 0; attributeIdx < sampleNodeAttributes.getLength(); attributeIdx++) {
                                        Node attributeNode = sampleNodeAttributes.item(attributeIdx);
                                        String name = attributeNode.getNodeName();
                                        // Sample nodes contain three information properties by just attaching something as a parent/children
                                        boolean isInfoProperty = name.equals("permId") || name.equals("identifier") || name.equals("sampleType");
                                        // If is not an information property is an annotation
                                        if (!isInfoProperty) {
                                            String value = attributeNode.getNodeValue();
                                            // If the annotations belongs to a parent or children needs to be checked
                                            boolean isParent = contains(samplePermId, sample.getParents());
                                            boolean isChildren = contains(samplePermId, sample.getChildren());
                                            // Add annotations to Sample Update
                                            if (isParent || isChildren) {
                                                if (sampleUpdate == null) {
                                                    sampleUpdate = new SampleUpdate();
                                                    sampleUpdate.setSampleId(sample.getPermId());
                                                }
                                                if (isParent) {
                                                    sampleUpdate.relationship(new SamplePermId(samplePermId)).addParentAnnotation(name, value);
                                                }
                                                if (isChildren) {
                                                    sampleUpdate.relationship(new SamplePermId(samplePermId)).addChildAnnotation(name, value);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (sampleUpdate != null) {
                    sampleUpdates.add(sampleUpdate);
                }
            }
            from = from + count;
        }

        System.out.println("ELNAnnotationsMigration sampleUpdates.size: " + sampleUpdates.size());
        api.updateSamples(sessionToken, sampleUpdates);

        System.out.println("ELNAnnotationsMigration beforeUpgrade END");
    }
}