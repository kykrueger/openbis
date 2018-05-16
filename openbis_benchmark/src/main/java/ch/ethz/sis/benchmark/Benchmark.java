package ch.ethz.sis.benchmark;

import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;
import ch.ethz.sis.ssl.SslCertificateHelper;

public abstract class Benchmark
{
	protected BenchmarkConfig configuration;
    protected Logger logger;
    
    public void start() {
    		logger = LogManager.getLogger(this.getClass());
    		long start = System.currentTimeMillis();
    		logger.traceAccess(null, configuration);
    		try {
    			startInternal();
    		} catch(Throwable throwable) {
    			logger.catching(throwable);
    		}
    		logger.traceExit(configuration);
    		long end = System.currentTimeMillis();
    		logger.info("Benchmark took: " + (end-start) + " millis");
    }
    public abstract void startInternal() throws Exception;
    
    public BenchmarkConfig getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(BenchmarkConfig serviceConfig)
    {
        this.configuration = serviceConfig;
        SslCertificateHelper.trustAnyCertificate(getConfiguration().getOpenbisURL());
        SslCertificateHelper.trustAnyCertificate(getConfiguration().getDatastoreURL());
    }

}
