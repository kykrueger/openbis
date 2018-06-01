package ch.ethz.sis.benchmark.impl;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.util.RandomWord;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;

public class GlobalSearchBenchmark extends Benchmark {

	private enum Parameters { ITERATIONS, THREADS }
	
	@Override
	public void startInternal() throws Exception {
        int iterations = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.ITERATIONS.name()));
        long laps = 0;
        for(int i = 0; i < iterations; i++) {
	    		String word = RandomWord.getRandomWord();
	    		GlobalSearchCriteria criteria = new GlobalSearchCriteria();
	    		criteria.withText().thatContains(word);
	        
	        GlobalSearchObjectFetchOptions options = new GlobalSearchObjectFetchOptions();
	        options.from(0);
	        options.count(25);
	        
	        	SampleFetchOptions sampleFetchOptions = options.withSample();
			sampleFetchOptions.withSpace();
			sampleFetchOptions.withType();
			sampleFetchOptions.withRegistrator();
			sampleFetchOptions.withModifier();
			sampleFetchOptions.withExperiment();
			sampleFetchOptions.withProperties();
			
			ExperimentFetchOptions experimentFetchOptions = options.withExperiment();
			experimentFetchOptions.withType();
			experimentFetchOptions.withRegistrator();
			experimentFetchOptions.withModifier();
			experimentFetchOptions.withProperties();
			
			DataSetFetchOptions dataSetFetchOptions = options.withDataSet();
			dataSetFetchOptions.withType();
			dataSetFetchOptions.withRegistrator();
			dataSetFetchOptions.withModifier();
			dataSetFetchOptions.withProperties();
			
			login();
	        long start = System.currentTimeMillis();
	        SearchResult<GlobalSearchObject> objects = v3.searchGlobally(sessionToken, criteria, options);
	        long end = System.currentTimeMillis();
	        long lap = end - start;
	        laps += lap;
	        logout();
	        logger.info("Found " + objects.getTotalCount() + " objects containting '" + word+ "' in " + lap + " millis.");
        }
        
        logger.info("Done " + iterations + " global searches in " + laps + " millis, " + (laps/iterations) + " millis/search avg");
	}

}
