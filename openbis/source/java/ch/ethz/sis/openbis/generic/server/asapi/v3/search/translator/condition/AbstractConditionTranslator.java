package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

public abstract class AbstractConditionTranslator<CRITERIA extends ISearchCriteria> implements IConditionTranslator<CRITERIA> {

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(CRITERIA criterion, TableMapper tableMapper,
            final IAliasFactory aliasFactory) {
        return null;
    }

}
