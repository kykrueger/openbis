package ch.ethz.sis.benchmark;

import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;

public abstract class Benchmark
{
	protected BenchmarkConfig benchmarkConfig;
    protected Logger logger;
    
    public void start() {
    		logger = LogManager.getLogger(this.getClass());
    		long start = System.currentTimeMillis();
    		logger.traceAccess(null, benchmarkConfig);
    		startInternal();
    		logger.traceExit(benchmarkConfig);
    		long end = System.currentTimeMillis();
    		logger.info("Benchmark took: " + (end-start) + " millis");
    }
    public abstract void startInternal();
    
    public BenchmarkConfig getConfiguration()
    {
        return benchmarkConfig;
    }

    public void setConfiguration(BenchmarkConfig serviceConfig)
    {
        this.benchmarkConfig = serviceConfig;
    }

}
