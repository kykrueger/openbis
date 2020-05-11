package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;

import java.util.*;

/*
 * The goal of this class is to replace substitute HibernateSearchDAO
 * This should make possible to remove Hibernate Search without changing all the other business layers
 */
public class HibernateSearchDAOV3Adaptor implements IHibernateSearchDAO {

    /**
     * The <code>Logger</code> of this class.
     * <p>
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HibernateSearchDAOV3Adaptor.class);

    //
    // IHibernateSearchDAO interface
    //

    @Override
    public List<MatchingEntity> searchEntitiesByTerm(String userId, SearchableEntity searchableEntity, String searchTerm, HibernateSearchDataProvider dataProvider, boolean useWildcardSearchMode, int alreadyFoundEntities, int maxSize) throws DataAccessException {
        throw new UnsupportedOperationException();
    }


    @Override
    public int getResultSetSizeLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setProperties(Properties serviceProperties) {
        for (String propertyName:serviceProperties.stringPropertyNames()) {
            operationLog.warn("Configuration property ignored: " + propertyName + " By: " + HibernateSearchDAOV3Adaptor.class.getSimpleName());
        }
    }

    @Override
    public List<Long> searchForEntityIds(final String userId,
                                         final DetailedSearchCriteria mainV1Criteria,
                                         final EntityKind entityKind,
                                         final List<IAssociationCriteria> associations)
    {
        // Obtain entity criteria
        AbstractEntitySearchCriteria mainV3Criteria = getCriteria(entityKind);

        // Entity Criteria translation
        for (DetailedSearchCriterion mainV1Criterion:mainV1Criteria.getCriteria()) {
            adapt(mainV3Criteria, mainV1Criteria, mainV1Criterion);
        }

        // Nested Criteria translation
        for (DetailedSearchSubCriteria subV1CriteriaPointer:mainV1Criteria.getSubCriterias()) {
            DetailedSearchCriteria subV1Criteria = subV1CriteriaPointer.getCriteria();

            for (DetailedSearchCriterion subV1Criterion:subV1Criteria.getCriteria()) {
                AbstractEntitySearchCriteria subV3Criteria = null;

                switch (subV1CriteriaPointer.getTargetEntityKind()) {
                    case SAMPLE:
                            if (mainV3Criteria instanceof DataSetSearchCriteria) {
                                subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withSample();
                            }
                        break;
                    case EXPERIMENT:
                            if (mainV3Criteria instanceof SampleSearchCriteria) {
                                subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withExperiment();
                            } else if(mainV3Criteria instanceof DataSetSearchCriteria) {
                                subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withExperiment();
                            }
                        break;
                    case DATA_SET:
                        // Seems unsupported on V3 Criteria
                        // Samples and Experiments don't have withDataSet
                        // Data Sets don't have withContained
                        break;
                    case DATA_SET_PARENT: // For Data Sets
                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
                            subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withParents();
                        }
                        break;
                    case DATA_SET_CHILD: // For Data Sets
                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
                            subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withChildren();
                        }
                        break;
                    case DATA_SET_CONTAINER: // For Data Sets
                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
                            subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withContainer();
                        }
                        break;
                    case SAMPLE_CONTAINER: // For Samples
                        if (mainV3Criteria instanceof SampleSearchCriteria) {
                            subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withContainer();
                        }
                        break;
                    case SAMPLE_CHILD: // For Samples
                        if (mainV3Criteria instanceof SampleSearchCriteria) {
                            subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withChildren();
                        }
                        break;
                    case SAMPLE_PARENT: // For Samples
                        if (mainV3Criteria instanceof SampleSearchCriteria) {
                            subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withParents();
                        }
                        break;
                    case MATERIAL:
                        // Seems unsupported on V3 Criteria
                        break;
                }

                if (subV3Criteria == null) {
                    throwUnsupportedOperationException("TargetEntityKind: " + subV1CriteriaPointer.getTargetEntityKind() + " For V3: " + mainV3Criteria.getClass().getName());
                }
                adapt(subV3Criteria, subV1Criteria, subV1Criterion);
            }
        }

        // Obtain entity id results from search manager
        Set<Long> results = getSearchManager(entityKind).searchForIDs(getUserId(userId),
                getAuthorisationInformation(userId),
                mainV3Criteria,
                null,
                null,
                ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN);

        return new ArrayList<>(results);
    }

    private void adapt(AbstractEntitySearchCriteria v3Criteria,
                       DetailedSearchCriteria v1Criteria,
                       DetailedSearchCriterion v1Criterion) {

        // Feature not supported on V3
        if(v1Criterion.isNegated()) {
            throwUnsupportedOperationException("negated");
        }

        for (String value:v1Criterion.getValues()) {
            ISearchCriteria criterionV3Criteria = null;
            switch (v1Criterion.getField().getKind()) {
                case ANY_FIELD:
                    criterionV3Criteria = v3Criteria.withAnyField();
                    break;
                case ANY_PROPERTY:
                    criterionV3Criteria = v3Criteria.withAnyProperty();
                    break;
                case PROPERTY:
                    if (v1Criterion.getField().getPropertyCode() == null) {
                        throw throwIllegalArgumentException("PROPERTY PropertyCode null");
                    }
                    if (isNumberProperty(v1Criterion.getField().getPropertyCode())) {
                        criterionV3Criteria = v3Criteria.withNumberProperty(v1Criterion.getField().getPropertyCode());
                    } else {
                        criterionV3Criteria = v3Criteria.withProperty(v1Criterion.getField().getPropertyCode());
                    }
                    break;
                case ATTRIBUTE:
                    if (v1Criterion.getField().getAttributeCode() == null) {
                        throw throwIllegalArgumentException("ATTRIBUTE AttributeCode null");
                    }
                    SearchCriteria.MatchClauseAttribute attributeCode = SearchCriteria.MatchClauseAttribute.valueOf(v1Criterion.getField().getAttributeCode());
                    switch (attributeCode) {
                        // common

                        case CODE:
                            criterionV3Criteria = v3Criteria.withCode();
                            break;
                        case TYPE:
                            criterionV3Criteria = withTypeCode(v3Criteria);
                            break;
                        case PERM_ID:
                            criterionV3Criteria = v3Criteria.withPermId();
                            break;
                        case METAPROJECT:
                            criterionV3Criteria = v3Criteria.withTag().withCode();
                            break;
                        case REGISTRATOR_USER_ID:
                            criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                            break;
                        case REGISTRATOR_FIRST_NAME:
                            criterionV3Criteria = v3Criteria.withRegistrator().withFirstName();
                            break;
                        case REGISTRATOR_LAST_NAME:
                            criterionV3Criteria = v3Criteria.withRegistrator().withLastName();
                            break;
                        case REGISTRATOR_EMAIL:
                            criterionV3Criteria = v3Criteria.withRegistrator().withEmail();
                            break;
                        case MODIFIER_USER_ID:
                            criterionV3Criteria = v3Criteria.withModifier().withUserId();
                            break;
                        case MODIFIER_FIRST_NAME:
                            criterionV3Criteria = v3Criteria.withModifier().withFirstName();
                            break;
                        case MODIFIER_LAST_NAME:
                            criterionV3Criteria = v3Criteria.withModifier().withLastName();
                            break;
                        case MODIFIER_EMAIL:
                            criterionV3Criteria = v3Criteria.withModifier().withEmail();
                            break;

                        // for sample or experiment

                        case SPACE:
                            criterionV3Criteria = withSpaceCode(v3Criteria);
                            break;

                        // for experiment

                        case PROJECT:
                            criterionV3Criteria = withProjectCode(v3Criteria);
                            break;
                        case PROJECT_PERM_ID:
                            criterionV3Criteria = withProjectPermId(v3Criteria);
                            break;

                    }
                    break;
                case REGISTRATOR:
                    break;
            }

            CompareType comparisonOperator = v1Criterion.getType();

            if (criterionV3Criteria instanceof StringFieldSearchCriteria) {
                StringFieldSearchCriteria stringFieldSearchCriteria = (StringFieldSearchCriteria) criterionV3Criteria;
                if (comparisonOperator == CompareType.EQUALS) {
                    // TODO: The V3 doesn't differentiate with or without wildcards, instead if a * or ? is found, they are used as wildcards
                    if (v1Criteria.isUseWildcardSearchMode()) {
                        stringFieldSearchCriteria.thatContains(value);
                    } else {
                        // Old Lucene behaviour was always word match, real equals should be even more restrictive/correct
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
