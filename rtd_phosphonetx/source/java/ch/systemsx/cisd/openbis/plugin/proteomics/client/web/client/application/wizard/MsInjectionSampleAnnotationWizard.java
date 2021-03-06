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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.Wizard;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * Wizard for annotation MS_INJECTION samples.
 *
 * @author Franz-Josef Elmer
 */
public class MsInjectionSampleAnnotationWizard extends Wizard<MsInjectionSampleAnnotationModel>
{
    public MsInjectionSampleAnnotationWizard(IViewContext<IPhosphoNetXClientServiceAsync> context)
    {
        super(new MsInjectionSampleAnnotationModel(context));
        MsInjectionSampleAnnotationModel wizardDataModel = getWizardDataModel();
        register(new MsInjectionSampleChoosingPage(context, wizardDataModel));
        register(new QuestionPage(context, wizardDataModel));
        register(new BiologicalSampleChoosingPage(context, wizardDataModel));
        register(new BiologicalSampleCreatingPage(context, wizardDataModel));
        start();
    }

}
