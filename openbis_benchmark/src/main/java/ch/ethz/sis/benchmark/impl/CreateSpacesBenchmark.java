package ch.ethz.sis.benchmark.impl;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class CreateSpacesBenchmark extends Benchmark {
	
	private enum Parameters { SPACES_TO_CREATE }
	
	@Override
	public void startInternal() {
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, getConfiguration().getOpenbisURL(), getConfiguration().getOpenbisTimeout());
        String sessionToken = v3.login(getConfiguration().getUser(), getConfiguration().getPassword());
        int spacesToCreate = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.SPACES_TO_CREATE.name()));
        List<SpaceCreation> spaceCreations = new ArrayList<SpaceCreation>();
        for(int i = 0; i < spacesToCreate; i++) {
        		SpaceCreation spaceCreation = new SpaceCreation();
        		spaceCreation.setCode("SPACE_" + (i+20000));
        		spaceCreations.add(spaceCreation);
        }
        v3.createSpaces(sessionToken, spaceCreations);
	}

}
