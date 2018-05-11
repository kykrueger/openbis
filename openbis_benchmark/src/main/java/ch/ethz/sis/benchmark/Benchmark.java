package ch.ethz.sis.benchmark;

import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;

public abstract class Benchmark
{
    private BenchmarkConfig benchmarkConfig;
    private static Logger logger = LogManager.getLogger(Benchmark.class);
    
    public void start() {
    		long start = System.currentTimeMillis();
    		logger.traceAccess("Starting Benchmark:", benchmarkConfig);
    		startInternal();
    		logger.traceExit(benchmarkConfig);
    		long end = System.currentTimeMillis();
    		logger.info("Benchmark took: " + (end-start) + " millis", null);
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
