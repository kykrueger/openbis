package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

/**
 * An enum listing the different kinds of entities that are searchable.
 * 
 * @author Piotr Buczek
 */
public enum SearchableEntityKind
{
    SAMPLE, EXPERIMENT, DATA_SET, MATERIAL,
    // sample subcriteria
    SAMPLE_CONTAINER, SAMPLE_PARENT, SAMPLE_CHILD,
    // data set subcriteria
    DATA_SET_CONTAINER, DATA_SET_PARENT, DATA_SET_CHILD
}