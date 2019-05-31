package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;

public abstract class AbstractConditionTranslator<CRITERIA extends ISearchCriteria> implements IConditionTranslator<CRITERIA> {
    @Override
    public JoinInformation getJoinInformation(ISearchCriteria criterion, EntityMapper entityMapper) {
        return null;
    }
}
