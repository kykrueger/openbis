package ch.ethz.sis.benchmark.impl;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.util.RandomValueGenerator;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class ListBenchmark extends Benchmark {

	private enum Parameters { ITERATIONS, THREADS }
	
	@Override
	public void startInternal() throws Exception {
		long start1 = System.currentTimeMillis();
		IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
        String sessionToken = v3.login(getConfiguration().getUser(), getConfiguration().getPassword());
        
        int iterations = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.ITERATIONS.name()));
        int threads = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.THREADS.name()));
        
        long start2 = System.currentTimeMillis();
        List<Space> spaces = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
        long end2 = System.currentTimeMillis();
        logger.info("Found " + spaces.size() + " spaces in " + (end2-start2) + " millis.");
        final RandomValueGenerator<Space> random = new RandomValueGenerator<>();
        random.addAll(spaces);
        
        List<Thread> threadsToJoin = new ArrayList<>();
        for(int t = 0; t < threads; t++) {
        		Thread thread = new Thread() {
        			public void run() {
        				for(int i = 0; i < iterations; i++) {
        					Space space = random.getRandom();
        					
        					ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
        					experimentSearchCriteria.withProject().withSpace().withCode().thatEquals(space.getCode());
        					
        					ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        					
        					long start1 = System.currentTimeMillis();
        					Experiment experiment = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions).getObjects().get(0);
        					long end1 = System.currentTimeMillis();
	        	            logger.info("Found experiment '" + experiment.getPermId().getPermId()+ "' from space " + space.getCode() + " in " + (end1-start1) + " millis.");
	        	            
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
	        	            
	        	            long start2 = System.currentTimeMillis();
	        	            SearchResult<Sample> samples = v3.searchSamples(sessionToken, criteria, options);
	        	            long end2 = System.currentTimeMillis();
	        	            logger.info("Found " + samples.getTotalCount() + " objects in experiment '" + experiment.getPermId().getPermId()+ "' from space " + space.getCode() + " in " + (end2-start2) + " millis.");
        				}
        			}
        		};
        		threadsToJoin.add(thread);
        		thread.start();
        }
        
        for(Thread thread:threadsToJoin) {
        		thread.join();
        }
        long end1 = System.currentTimeMillis();
        logger.info("Done " + (iterations * threads) + " sample searches in " + (end1-start1) + " millis, " + ((end1-start1)/(iterations * threads)) + " millis/search");
	}

}
