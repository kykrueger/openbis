package ch.ethz.sis.benchmark.impl;

import java.util.Collections;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.util.RandomValueGenerator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;

public class ListBenchmark extends Benchmark {

	private enum Parameters { ITERATIONS, THREADS }
	
	@Override
	public void startInternal() throws Exception {
		login();
        int iterations = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.ITERATIONS.name()));
        long start = System.currentTimeMillis();
        List<Space> spaces = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
        long end = System.currentTimeMillis();
        logger.info("Found " + spaces.size() + " spaces in " + (end-start) + " millis.");
        final RandomValueGenerator<Space> random = new RandomValueGenerator<>();
        random.addAll(spaces);
        logout();
        
        long laps = 0;
        for(int i = 0; i < iterations; i++) {
        		login();
			
        		List<Experiment> experiments = Collections.emptyList();
        		Space space = null;
        		long lapStart1 = System.currentTimeMillis();
			while(experiments.isEmpty()) {
				space = random.getRandom();
				ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
				experimentSearchCriteria.withProject().withSpace().withCode().thatEquals(space.getCode());
				ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
				experiments = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions).getObjects();
			}
			long lapEnd1 = System.currentTimeMillis();
			Experiment experiment = experiments.get(0);
			logger.info("Found experiment '" + experiment.getPermId().getPermId()+ "' from space " + space.getCode() + " in " + (lapEnd1-lapStart1) + " millis.");
			
        		SampleSearchCriteria criteria = new SampleSearchCriteria();
        		criteria.withExperiment().withPermId().thatEquals(experiment.getPermId().getPermId());
            
            SampleFetchOptions options = new SampleFetchOptions();
            options.from(0);
            options.count(25);
            options.withType();
            options.withSpace();
            options.withRegistrator();
            options.withModifier();
            options.withProperties();
            options.withParents();
            options.withChildren();
            
            long lapStart2 = System.currentTimeMillis();
            SearchResult<Sample> samples = v3.searchSamples(sessionToken, criteria, options);
            long lapEnd2 = System.currentTimeMillis();
            long lap = lapEnd2 - lapStart2;
            laps += lap;
            logger.info("Found " + samples.getTotalCount() + " objects in experiment '" + experiment.getPermId().getPermId()+ "' from space " + space.getCode() + " in " + lap + " millis.");
            logout();
        }
        logger.info("Done " + (iterations) + " experiment object lists in " + laps + " millis, " + ((laps)/(iterations)) + " millis/search avg");
	}

}
