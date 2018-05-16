package ch.ethz.sis.benchmark.impl;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.util.RandomWord;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class GlobalSearchBenchmark extends Benchmark {

	private enum Parameters { ITERATIONS, THREADS }
	
	@Override
	public void startInternal() throws Exception {
		long start = System.currentTimeMillis();
		IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
        String sessionToken = v3.login(getConfiguration().getUser(), getConfiguration().getPassword());
        
        int iterations = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.ITERATIONS.name()));
        int threads = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.THREADS.name()));
        
        List<Thread> threadsToJoin = new ArrayList<>();
        for(int t = 0; t < threads; t++) {
        		Thread thread = new Thread() {
        			public void run() {
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
	        				
	        	            long start = System.currentTimeMillis();
	        	            SearchResult<GlobalSearchObject> objects = v3.searchGlobally(sessionToken, criteria, options);
	        	            long end = System.currentTimeMillis();
	        	            logger.info("Found " + objects.getTotalCount() + " objects containting '" + word+ "' in " + (end-start) + " millis.");
        				}
        			}
        		};
        		threadsToJoin.add(thread);
        		thread.start();
        }
        
        for(Thread thread:threadsToJoin) {
        		thread.join();
        }
        long end = System.currentTimeMillis();
        logger.info("Done " + (iterations * threads) + " objects searches in " + (end-start) + " millis, " + ((end-start)/(iterations * threads)) + " millis/search");
	}

}
