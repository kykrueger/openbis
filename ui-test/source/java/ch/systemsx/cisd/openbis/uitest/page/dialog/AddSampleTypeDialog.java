/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.dialog;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

public class AddSampleTypeDialog extends Page
{

    @Locate("openbis_dialog-code-field")
    private Text code;

    @Locate("openbis_add-type-dialog-description-field")
    private Text description;

    @Locate("openbis_add-type-dialog-listable")
    private Checkbox listable;

    @Locate("openbis_add-type-dialog-show-container")
    private Checkbox showContainer;

    @Locate("openbis_add-type-dialog-show-parents")
    private Checkbox showParents;

    @Locate("openbis_add-type-dialog-subcode-unique")
    private Checkbox uniqueSubcodes;

    @Locate("openbis_add-type-dialog-autogenerated-code")
    private Checkbox generateCodesAutomatically;

    @Locate("openbis_add-type-dialog-show-parent-metadata")
    private Checkbox showParentMetadata;

    @Locate("openbis_add-type-dialog-generated-code-prefix")
    private Text generatedCodePrefix;

    @Locate("openbis_dialog-save-button")
    private Button save;

    @Locate("openbis_dialog-cancel-button")
    private Button cancel;

    public SampleTypeBrowser save()
    {
        save.click();
        return get(SampleTypeBrowser.class);
    }

    public SampleTypeBrowser cancel()
    {
        cancel.click();
        return get(SampleTypeBrowser.class);
    }

    public AddSampleTypeDialog setCode(String code)
    {
        this.code.write(code);
        return this;
    }

    public AddSampleTypeDialog setListable(boolean checked)
    {
        listable.set(checked);
        return this;
    }

    public AddSampleTypeDialog setShowContainer(boolean checked)
    {
        showContainer.set(checked);
        return this;
    }

    public AddSampleTypeDialog setShowParents(boolean checked)
    {
        showParents.set(checked);
        return this;
    }

    public void fillWith(SampleType sampleType)
    {
        setCode(sampleType.getCode());
        setListable(sampleType.isListable());
        setShowContainer(sampleType.isShowContainer());
        setShowParents(sampleType.isShowParents());
    }

}
