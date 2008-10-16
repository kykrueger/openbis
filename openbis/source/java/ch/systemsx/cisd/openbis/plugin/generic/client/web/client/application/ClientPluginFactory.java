/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Collections;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.SampleTypeCode;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory implements IClientPluginFactory
{

    //
    // IClientPluginFactory
    //

    public final ISampleViewClientPlugin createViewClientForSampleType(final String sampleTypeCode)
    {
        return new SampleViewClientPlugin();
    }

    public final Set<String> getSampleTypeCodes()
    {
        return Collections.singleton(SampleTypeCode.CONTROL_LAYOUT.getCode());
    }

    //
    // Helper classes
    //

    private final static class SampleViewClientPlugin implements ISampleViewClientPlugin
    {

        //
        // ISampleViewClientPlugin
        //

        public final void viewSample(final String sampleIdentifier)
        {
            MessageBox.alert("Generic", sampleIdentifier, null);
        }
    }
}
