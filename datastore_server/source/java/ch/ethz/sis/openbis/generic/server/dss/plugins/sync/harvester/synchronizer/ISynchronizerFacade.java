package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;

public interface ISynchronizerFacade {
	public void updateFileFormatType(AbstractType type);
	
	public void registerFileFormatType(FileFormatType type);
	
    public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment);

    public void assignPropertyType(NewETPTAssignment newETPTAssignment);
    
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode);
    
    public void updatePropertyType(PropertyType propertyType);
    public void registerPropertyType(PropertyType propertyType);
    public void updateValidationPlugin(Script script);
    public void registerValidationPlugin(Script script);

    public void registerVocabulary(NewVocabulary vocab);

    public void updateVocabulary(Vocabulary vocab);
    
    public void registerSampleType(SampleType sampleType);
    public void registerDataSetType(DataSetType dataSetType);
    public void registerExperimentType(ExperimentType experimentType);
    public void registerMaterialType(MaterialType materialType);

	public void updateVocabularyTerm(VocabularyTerm term);

	public void updateSampleType(EntityType incomingEntityType);

	public void updateDataSetType(EntityType incomingEntityType);

	public void updateExperimentType(EntityType incomingEntityType);

	public void updateMaterialType(EntityType incomingEntityType);

	public void addVocabularyTerms(String code, TechId techId, List<VocabularyTerm> termsToBeAdded);

	public void deleteVocabularyTerms(TechId vocabularyId, String code, List<VocabularyTerm> list,
			List<VocabularyTermReplacement> emptyList);
	
	public void printSummary();
}
