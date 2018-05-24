package ch.ethz.sis.benchmark;

import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.ssl.SslCertificateHelper;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public abstract class Benchmark
{
	protected BenchmarkConfig configuration;
    protected Logger logger;
    protected IApplicationServerApi v3;
    protected String sessionToken;
    
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
    
    public IApplicationServerApi login() {
    		if(v3 == null) {
    			v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
    	        sessionToken = v3.login(getConfiguration().getUser(), getConfiguration().getPassword());
    		}
        return v3;
    }
    
    public void logout() {
    		v3.logout(sessionToken);
    		v3 = null;
    		sessionToken = null;
    }
    
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
