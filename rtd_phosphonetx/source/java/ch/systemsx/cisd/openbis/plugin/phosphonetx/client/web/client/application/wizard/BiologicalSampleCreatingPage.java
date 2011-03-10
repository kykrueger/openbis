/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.wizard;

import com.extjs.gxt.ui.client.widget.Html;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardPage;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * Wizard for guiding the user to annotate an MS_INJECTION sample.
 *
 * @author Franz-Josef Elmer
 */
public class BiologicalSampleCreatingPage extends WizardPage<MsInjectionSampleAnnotationModel>
{

    public BiologicalSampleCreatingPage(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            MsInjectionSampleAnnotationModel model)
    {
        super(viewContext, MsInjectionAnnotationWizardState.BIOLOGICAL_SAMPLE_CREATING, model);
        setLeftContentBy(new Html(
                "Annotating <tt>MS_INJECTION</tt> sample by creating a new biological sample means ..."));
    }

}
