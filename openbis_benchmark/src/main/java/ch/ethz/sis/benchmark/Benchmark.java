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
    
    protected long maxOpTime = Long.MIN_VALUE;
    protected long minOpTime = Long.MAX_VALUE;
    protected long numOps = 0;
    protected long totalOpTime = 0;
    
    protected void addOperation(long start, long end) {
    		long total = end - start;
    		totalOpTime += total;
    		numOps++;
    		if(total < minOpTime) {
    			minOpTime = total;
    		}
    		if(total > maxOpTime) {
    			maxOpTime = total;
    		}
    }
    
    public void start() {
    		logger = LogManager.getLogger(this.getClass());
    		logger.traceAccess(null, configuration);
    		try {
    			startInternal();
    		} catch(Throwable throwable) {
    			logger.catching(throwable);
    		}
    		logger.traceExit(configuration);
    		if(numOps > 0) {
    			logger.info("totalOpTime: " + totalOpTime + " numOps: " + numOps + " avgOpTime: " + (totalOpTime/numOps) + " maxOpTime: " + maxOpTime + " minOpTime: " + minOpTime);
    		} else {
    			logger.info("no operations where done");
    		}
    		
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

	public long getMaxOpTime() {
		return maxOpTime;
	}

	public long getMinOpTime() {
		return minOpTime;
	}

	public long getNumOps() {
		return numOps;
	}

	public long getTotalOpTime() {
		return totalOpTime;
	}

	public long getAVGOpTime() {
		return totalOpTime/numOps;
	}
}
