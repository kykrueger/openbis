package ch.ethz.sis.benchmark;

import ch.ethz.sis.benchmark.impl.IApplicationServerApiWrapper;
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
	protected IApplicationServerApiWrapper v3Wrapper;
	protected String sessionToken;
    
    protected long maxOpTime = Long.MIN_VALUE;
    protected long minOpTime = Long.MAX_VALUE;
    protected long numOps = 0;
    protected long totalOpTime = 0;
    
    protected void addOperation(long start, long end, int size) {
    		long total = end - start;
    		totalOpTime += total;
    		numOps++;
    		if(total < minOpTime) {
    			minOpTime = total;
    		}
    		if(total > maxOpTime) {
    			maxOpTime = total;
    		}
    		logger.info("REPORT SINGLE\t" +  size + "\t" + total);
    }
    
    public void start() {
    		logger = LogManager.getLogger(this.getClass());
    		logger.traceAccess(null, configuration);
    		logger.info("REPORT THREAD\ttotalOpTime\tnumOps\tavgOpTime\tmaxOpTime\tminOpTime");
    		logger.info("REPORT SINGLE\topSize\topTime");
    		try {
    			startInternal();
    		} catch(Throwable throwable) {
    			logger.catching(throwable);
    		}
    		logger.traceExit(configuration);
    		if(numOps > 0) {
    			logger.info("REPORT THREAD\t" + totalOpTime + "\t" + numOps + "\t" + (totalOpTime/numOps) + "\t" + maxOpTime + "\t" + minOpTime);
    		} else {
    			logger.info("REPORT THREAD\tNO-OP");
    		}
    		
    }
    
    public abstract void startInternal() throws Exception;
    
    public IApplicationServerApi login() {
    		if(v3 == null) {
    			v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
    			if (v3Wrapper != null) {
    				v3Wrapper.setInstance(v3);
    				v3 = v3Wrapper;
    				v3Wrapper = null;
				}
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
