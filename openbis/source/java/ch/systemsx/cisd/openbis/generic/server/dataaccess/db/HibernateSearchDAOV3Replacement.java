package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * The goal of this class is to replace substitute HibernateSearchDAO
 * This should make possible to remove Hibernate Search without changing all the other business layers
 */
public class HibernateSearchDAOV3Replacement {

    public List<Long> searchForEntityIds(final String userId,
                                                final DetailedSearchCriteria legacySearchCriteria,
                                                final EntityKind entityKind,
                                                final List<IAssociationCriteria> associations)
    {
        // Obtain entity criteria
        AbstractEntitySearchCriteria criteria = getCriteria(entityKind);

        // Entity Criteria translation
        for (DetailedSearchCriterion criterion:legacySearchCriteria.getCriteria()) {

            // Feature not supported on V3
            if(criterion.isNegated()) {
                throwUnsupportedOperationException("negated");
            }

            for (String value:criterion.getValues()) {
                ISearchCriteria criterionV3Criteria = null;
                switch (criterion.getField().getKind()) {
                    case ANY_FIELD:
                        criterionV3Criteria = criteria.withAnyField();
                        break;
                    case ANY_PROPERTY:
                        criterionV3Criteria = criteria.withAnyProperty();
                        break;
                    case PROPERTY:
                        if (criterion.getField().getPropertyCode() == null) {
                            throw throwIllegalArgumentException("PROPERTY PropertyCode null");
                        }
                        if (isNumberProperty(criterion.getField().getPropertyCode())) {
                            criterionV3Criteria = criteria.withNumberProperty(criterion.getField().getPropertyCode());
                        } else {
                            criterionV3Criteria = criteria.withProperty(criterion.getField().getPropertyCode());
                        }
                        break;
                    case ATTRIBUTE:
                        if (criterion.getField().getAttributeCode() == null) {
                            throw throwIllegalArgumentException("ATTRIBUTE AttributeCode null");
                        }
                        SearchCriteria.MatchClauseAttribute attributeCode = SearchCriteria.MatchClauseAttribute.valueOf(criterion.getField().getAttributeCode());
                        switch (attributeCode) {
                            // common

                            case CODE:
                                criterionV3Criteria = criteria.withCode();
                                break;
                            case TYPE:
                                criterionV3Criteria = withTypeCode(criteria);
                                break;
                            case PERM_ID:
                                criterionV3Criteria = criteria.withPermId();
                                break;
                            case METAPROJECT:
                                criterionV3Criteria = criteria.withTag().withCode();
                                break;
                            case REGISTRATOR_USER_ID:
                                criterionV3Criteria = criteria.withRegistrator().withUserId();
                                break;
                            case REGISTRATOR_FIRST_NAME:
                                criterionV3Criteria = criteria.withRegistrator().withFirstName();
                                break;
                            case REGISTRATOR_LAST_NAME:
                                criterionV3Criteria = criteria.withRegistrator().withLastName();
                                break;
                            case REGISTRATOR_EMAIL:
                                criterionV3Criteria = criteria.withRegistrator().withEmail();
                                break;
                            case MODIFIER_USER_ID:
                                criterionV3Criteria = criteria.withModifier().withUserId();
                                break;
                            case MODIFIER_FIRST_NAME:
                                criterionV3Criteria = criteria.withModifier().withFirstName();
                                break;
                            case MODIFIER_LAST_NAME:
                                criterionV3Criteria = criteria.withModifier().withLastName();
                                break;
                            case MODIFIER_EMAIL:
                                criterionV3Criteria = criteria.withModifier().withEmail();
                                break;

                            // for sample or experiment

                            case SPACE:
                                criterionV3Criteria = withSpaceCode(criteria);
                                break;

                            // for experiment

                            case PROJECT:
                                criterionV3Criteria = withProjectCode(criteria);
                                break;
                            case PROJECT_PERM_ID:
                                criterionV3Criteria = withProjectPermId(criteria);
                                break;

                        }
                        break;
                    case REGISTRATOR:
                        break;
                }

                CompareType comparisonOperator = criterion.getType();

                if (criterionV3Criteria instanceof StringFieldSearchCriteria) {
                    StringFieldSearchCriteria stringFieldSearchCriteria = (StringFieldSearchCriteria) criterionV3Criteria;
                    if (comparisonOperator == CompareType.EQUALS) {
                        // TODO: The V3 doesn't differentiate with or without wildcards, instead if a * or ? is found, they are used as wildcards
                        if (legacySearchCriteria.isUseWildcardSearchMode()) {
                            stringFieldSearchCriteria.thatContains(value);
                        } else {
                            // Old Lucene behaviour was always word match, real equals should be even more restrictive
                            stringFieldSearchCriteria.thatEquals(value);
                        }
                    } else {
                        throw throwIllegalArgumentException("non EQUALS compare type for StringFieldSearchCriteria");
                    }
                } else if(criterionV3Criteria instanceof NumberPropertySearchCriteria) {
                    NumberFieldSearchCriteria numberFieldSearchCriteria = (NumberFieldSearchCriteria) criterionV3Criteria;
                    Number number = getNumber(value);
                    switch (comparisonOperator) {
                        case LESS_THAN:
                            numberFieldSearchCriteria.thatIsLessThan(number);
                            break;
                        case LESS_THAN_OR_EQUAL:
                            numberFieldSearchCriteria.thatIsLessThanOrEqualTo(number);
                            break;
                        case EQUALS:
                            numberFieldSearchCriteria.thatEquals(number);
                            break;
                        case MORE_THAN_OR_EQUAL:
                            numberFieldSearchCriteria.thatIsGreaterThanOrEqualTo(number);
                            break;
                        case MORE_THAN:
                            numberFieldSearchCriteria.thatIsGreaterThan(number);
                            break;
                    }
                }
            }
        }

        // Nested Criteria translation ?

        // Obtain entity id results from search manager
        Set<Long> results = getSearchManager(entityKind).searchForIDs(getUserId(userId),
                getAuthorisationInformation(userId),
                criteria,
                null,
                null,
                ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN);

        return new ArrayList<>(results);
    }

    //
    // Helper Methods - Criteria build
    //

    private AbstractEntitySearchCriteria getCriteria(EntityKind entityKind) {
        AbstractEntitySearchCriteria criteria = null;
        switch (entityKind) {
            case MATERIAL:
                criteria = new MaterialSearchCriteria();
                break;
            case EXPERIMENT:
                criteria = new ExperimentSearchCriteria();
                break;
            case SAMPLE:
                criteria = new SampleSearchCriteria();
                break;
            case DATA_SET:
                criteria = new DataSetSearchCriteria();
                break;
        }
        return criteria;
    }

    @Autowired
    private MaterialSearchManager materialSearchManager;
    @Autowired
    private ExperimentSearchManager experimentSearchManager;
    @Autowired
    private SampleSearchManager sampleSearchManager;
    @Autowired
    private DataSetSearchManager dataSetSearchManager;

    private ISearchManager getSearchManager(EntityKind entityKind) {
        ISearchManager manager = null;
        switch (entityKind) {
            case MATERIAL:
                manager = materialSearchManager;
                break;
            case EXPERIMENT:
                manager = experimentSearchManager;
                break;
            case SAMPLE:
                manager = sampleSearchManager;
                break;
            case DATA_SET:
                manager = dataSetSearchManager;
                break;
        }
        return manager;
    }

    private StringFieldSearchCriteria withTypeCode(AbstractEntitySearchCriteria entitySearchCriteria) {
        if (entitySearchCriteria instanceof SampleSearchCriteria) {
            return ((SampleSearchCriteria)entitySearchCriteria).withType().withCode();
        } else if (entitySearchCriteria instanceof ExperimentSearchCriteria) {
            return ((ExperimentSearchCriteria)entitySearchCriteria).withType().withCode();
        } else if (entitySearchCriteria instanceof DataSetSearchCriteria) {
            return ((DataSetSearchCriteria)entitySearchCriteria).withType().withCode();
        } else if (entitySearchCriteria instanceof MaterialSearchCriteria) {
            return ((MaterialSearchCriteria)entitySearchCriteria).withType().withCode();
        } else {
            throw throwIllegalArgumentException("withType Unreachable statement");
        }
    }

    private StringFieldSearchCriteria withSpaceCode(AbstractEntitySearchCriteria entitySearchCriteria) {
        if (entitySearchCriteria instanceof SampleSearchCriteria) {
            return ((SampleSearchCriteria)entitySearchCriteria).withProject().withSpace().withCode();
        } else if (entitySearchCriteria instanceof ExperimentSearchCriteria) {
            return ((ExperimentSearchCriteria)entitySearchCriteria).withProject().withSpace().withCode();
        } else {
            throw throwIllegalArgumentException("withSpace Unreachable statement");
        }
    }

    private StringFieldSearchCriteria withProjectCode(AbstractEntitySearchCriteria entitySearchCriteria) {
        if (entitySearchCriteria instanceof ExperimentSearchCriteria) {
            return ((ExperimentSearchCriteria)entitySearchCriteria).withProject().withCode();
        } else {
            throw throwIllegalArgumentException("withProjectCode Unreachable statement");
        }
    }

    private StringFieldSearchCriteria withProjectPermId(AbstractEntitySearchCriteria entitySearchCriteria) {
        if (entitySearchCriteria instanceof ExperimentSearchCriteria) {
            return ((ExperimentSearchCriteria)entitySearchCriteria).withProject().withPermId();
        } else {
            throw throwIllegalArgumentException("withProjectPermId Unreachable statement");
        }
    }

    //
    // Helper Methods - Numbers
    //

    private Number getNumber(String number) {
        try {
            if (isInteger(number)) {
                return Integer.parseInt(number);
            } else {
                return Double.parseDouble(number);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("NaN : " + number);
        }
    }

    private static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    private static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) {
            return false;
        }
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if(Character.digit(s.charAt(i),radix) < 0) {
                return false;
            }
        }
        return true;
    }

    //TODO - Not implemented
    private boolean isNumberProperty(String propertyCode) {
        return false;
    }

    //
    // Helper Methods - Authorisation
    //

    //TODO - Not implemented
    private Long getUserId(String userId) {
        return null;
    }

    //TODO - Not implemented
    private AuthorisationInformation getAuthorisationInformation(String userId) {
        return null;
    }

    //
    // Helper Methods - Errors
    //

    private RuntimeException throwUnsupportedOperationException(String feature) throws RuntimeException {
        return new RuntimeException(new UnsupportedOperationException("HibernateSearchDAOV3Replacement - Feature not supported: " + feature));
    }

    private RuntimeException throwIllegalArgumentException(String feature) throws RuntimeException {
        return new IllegalArgumentException("HibernateSearchDAOV3Replacement - Illegal argument: " + feature);
    }

}
