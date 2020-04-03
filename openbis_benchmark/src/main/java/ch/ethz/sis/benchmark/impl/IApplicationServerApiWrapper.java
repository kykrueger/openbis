package ch.ethz.sis.benchmark.impl;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public interface IApplicationServerApiWrapper extends IApplicationServerApi {
    void setInstance(IApplicationServerApi instance);
}
