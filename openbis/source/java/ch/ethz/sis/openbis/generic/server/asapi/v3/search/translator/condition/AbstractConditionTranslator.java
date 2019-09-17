package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;

import java.util.Map;

public abstract class AbstractConditionTranslator<CRITERIA extends ISearchCriteria> implements IConditionTranslator<CRITERIA> {

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(ISearchCriteria criterion, EntityMapper entityMapper,
            final IAliasFactory aliasFactory) {
        return null;
    }

}
