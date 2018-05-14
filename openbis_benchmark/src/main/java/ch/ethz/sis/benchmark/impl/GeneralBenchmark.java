package ch.ethz.sis.benchmark.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.util.RandomValueGenerator;
import ch.ethz.sis.benchmark.util.RandomWord;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class GeneralBenchmark extends Benchmark {
	
	private enum Parameters { SPACES_TO_CREATE, SAMPLES_TO_CREATE }
	
	@Override
	public void startInternal() {
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
        String sessionToken = v3.login(getConfiguration().getUser(), getConfiguration().getPassword());
        
        //
        // Setup - Create Type
        //
        EntityTypePermId sampleTypeCode = new EntityTypePermId("BENCHMARK_OBJECT");
        SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode(sampleTypeCode.getPermId());
        v3.createSampleTypes(sessionToken, Arrays.asList(sampleTypeCreation));
        
        //
        // Part 1 - Creating Spaces
        //
        long start1 = System.currentTimeMillis();
        RandomValueGenerator<String> spaceCodes = new RandomValueGenerator<String>();
        int spacesToCreate = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.SPACES_TO_CREATE.name()));
        List<SpaceCreation> spaceCreations = new ArrayList<SpaceCreation>();
        for(int i = 0; i < spacesToCreate; i++) {
        		SpaceCreation spaceCreation = new SpaceCreation();
        		String spaceCode = null;
        		while(spaceCode == null || spaceCodes.contains(spaceCode)) {
        			spaceCode = "SPACE_" + RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord();
        		}
        		spaceCodes.add(spaceCode);
        		spaceCreation.setCode(spaceCode);
        		spaceCreations.add(spaceCreation);
        }
        v3.createSpaces(sessionToken, spaceCreations);
        long end1 = System.currentTimeMillis();
        logger.info("Create " + spacesToCreate + " Spaces took: " + (end1-start1) + " millis - " + ((end1-start1)/spacesToCreate) + " millis/space");
        
        //
        // Part 2 - Creating Samples
        //
        long start2 = System.currentTimeMillis();
        long lapStart2 = System.currentTimeMillis();
        Set<String> sampleCodes = new HashSet<String>();
        int sampleBatchSize = 5000;
        int samplesToCreate = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.SAMPLES_TO_CREATE.name()));
        List<SampleCreation> sampleCreations = new ArrayList<SampleCreation>();
        for(int i = 0; i < samplesToCreate; i++) {
        		SampleCreation sampleCreation = new SampleCreation();
        		String sampleCode = null;
        		while(sampleCode == null || sampleCodes.contains(sampleCode)) {
        			sampleCode = "SAMPLE_" + RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord();
        		}
        		sampleCreation.setTypeId(sampleTypeCode);
        		sampleCreation.setCode(sampleCode);
        		sampleCreation.setSpaceId(new SpacePermId(spaceCodes.getRandom())); // Spaces are distributed randomly
        		sampleCreations.add(sampleCreation);
        		if(i % sampleBatchSize == 0) { // Every 5000, send to openBIS
        			v3.createSamples(sessionToken, sampleCreations);
        			long lapEnd2 = System.currentTimeMillis();
        			logger.info("Create " + sampleCreations.size() + " Samples took: " + (lapEnd2 - lapStart2) + " millis - " + ((lapEnd2-lapStart2)/sampleCreations.size()) + " millis/sample");
        			sampleCreations.clear();
        			lapStart2 = System.currentTimeMillis();
        		}
        }
        long end2 = System.currentTimeMillis();
        logger.info("Create " + samplesToCreate + " Samples took: " + (end2-start2) + " millis - " + ((end2-start2)/samplesToCreate) + " millis/sample");
        
	}

}
