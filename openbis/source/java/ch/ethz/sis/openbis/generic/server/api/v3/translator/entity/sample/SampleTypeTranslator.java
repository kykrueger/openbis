package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

public class SampleTypeTranslator extends AbstractCachingTranslator<SampleTypePE, SampleType, SampleTypeFetchOptions>
{
    public SampleTypeTranslator(TranslationContext translationContext, SampleTypeFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected SampleType createObject(SampleTypePE type)
    {
        SampleType result = new SampleType();

        result.setAutoGeneratedCode(type.isAutoGeneratedCode());
        result.setListable(type.isListable());
        result.setShowParentMetadata(type.isShowParentMetadata());
        result.setSubcodeUnique(type.isSubcodeUnique());
        result.setCode(type.getCode());
        result.setDescription(type.getDescription());
        result.setGeneratedCodePrefix(type.getGeneratedCodePrefix());
        result.setModificationDate(type.getModificationDate());
        result.setFetchOptions(new SampleTypeFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(SampleTypePE input, SampleType output)
    {

    }

}
