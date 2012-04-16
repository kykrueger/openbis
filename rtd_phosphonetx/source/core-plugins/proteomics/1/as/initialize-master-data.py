import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

file_type_HTML = tr.createNewFileFormatType('HTML')
file_type_HTML.setDescription('HTML File')

file_type_MZDATA = tr.createNewFileFormatType('MZDATA')
file_type_MZDATA.setDescription('Mass spectrometry data format.')

file_type_MZML = tr.createNewFileFormatType('MZML')
file_type_MZML.setDescription('Mass spectrometry data format. \
Unifiying mzXML and mzData formats, as released at the \
2008 American Society for Mass Spectrometry Meeting.')

file_type_MZXML = tr.createNewFileFormatType('MZXML')
file_type_MZXML.setDescription('Mass spectrometry data format.')

file_type_RAW = tr.createNewFileFormatType('RAW')
file_type_RAW.setDescription('Proprietary file format for Thermo mass sepectrometry data.')

file_type_TGZ = tr.createNewFileFormatType('TGZ')
file_type_TGZ.setDescription('gzipped tar')

file_type_WIFF = tr.createNewFileFormatType('WIFF')
file_type_WIFF.setDescription('Proprieatry file format for Sciex and Agilent mass spectrometry data.')

file_type_ZIP = tr.createNewFileFormatType('ZIP')
file_type_ZIP.setDescription('A zipped package')

vocabulary_TREATMENT_TYPE = tr.createNewVocabulary('TREATMENT_TYPE')
vocabulary_TREATMENT_TYPE.setDescription('Type of treatment of a biological sample.')
vocabulary_TREATMENT_TYPE.setUrlTemplate(None)
vocabulary_TREATMENT_TYPE.setManagedInternally(False)
vocabulary_TREATMENT_TYPE.setInternalNamespace(False)
vocabulary_TREATMENT_TYPE.setChosenFromList(True)

vocabulary_term_TREATMENT_TYPE_PH = tr.createNewVocabularyTerm('PH')
vocabulary_term_TREATMENT_TYPE_PH.setDescription(None)
vocabulary_term_TREATMENT_TYPE_PH.setUrl(None)
vocabulary_term_TREATMENT_TYPE_PH.setLabel('ph')
vocabulary_term_TREATMENT_TYPE_PH.setOrdinal(1)
vocabulary_TREATMENT_TYPE.addTerm(vocabulary_term_TREATMENT_TYPE_PH)

exp_type_BIOLOGICAL_EXPERIMENT = tr.createNewExperimentType('BIOLOGICAL_EXPERIMENT')
exp_type_BIOLOGICAL_EXPERIMENT.setDescription('A biological experiment')

exp_type_MS_INJECT = tr.createNewExperimentType('MS_INJECT')
exp_type_MS_INJECT.setDescription('MS injection experiment')

exp_type_MS_SEARCH = tr.createNewExperimentType('MS_SEARCH')
exp_type_MS_SEARCH.setDescription('MS_SEARCH experiment')

exp_type_MS_QUANTIFICATION = tr.createNewExperimentType('MS_QUANTIFICATION')
exp_type_MS_QUANTIFICATION.setDescription('Quantification of LC-MS data')

samp_type_MS_INJECTION = tr.createNewSampleType('MS_INJECTION')
samp_type_MS_INJECTION.setDescription('injection of a biological sample into a MS')
samp_type_MS_INJECTION.setListable(True)
samp_type_MS_INJECTION.setSubcodeUnique(False)
samp_type_MS_INJECTION.setAutoGeneratedCode(False)
samp_type_MS_INJECTION.setGeneratedCodePrefix('S')

samp_type_SEARCH = tr.createNewSampleType('SEARCH')
samp_type_SEARCH.setDescription('pointer to an MS_INJECTION sample used as placeholder for searches')
samp_type_SEARCH.setListable(True)
samp_type_SEARCH.setSubcodeUnique(False)
samp_type_SEARCH.setAutoGeneratedCode(False)
samp_type_SEARCH.setGeneratedCodePrefix('S')

data_set_type_MZXML_DATA = tr.createNewDataSetType('MZXML_DATA')
data_set_type_MZXML_DATA.setDescription('standardized format for ms data')
data_set_type_MZXML_DATA.setContainerType(False)

data_set_type_PROT_RESULT = tr.createNewDataSetType('PROT_RESULT')
data_set_type_PROT_RESULT.setDescription('protXML file')
data_set_type_PROT_RESULT.setContainerType(False)

data_set_type_RAW_DATA = tr.createNewDataSetType('RAW_DATA')
data_set_type_RAW_DATA.setDescription(None)
data_set_type_RAW_DATA.setContainerType(False)

prop_type_PARENTDATASETCODES = tr.createNewPropertyType('PARENT-DATA-SET-CODES', DataType.VARCHAR)
prop_type_PARENTDATASETCODES.setLabel('Parent Dataset Codes')
prop_type_PARENTDATASETCODES.setManagedInternally(False)
prop_type_PARENTDATASETCODES.setInternalNamespace(False)

prop_type_TREATMENT_TYPE1 = tr.createNewPropertyType('TREATMENT_TYPE1', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE1.setLabel('Treatment Type 1')
prop_type_TREATMENT_TYPE1.setManagedInternally(False)
prop_type_TREATMENT_TYPE1.setInternalNamespace(False)

prop_type_TREATMENT_TYPE1.setVocabulary(vocabulary_TREATMENT_TYPE)

prop_type_TREATMENT_TYPE2 = tr.createNewPropertyType('TREATMENT_TYPE2', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE2.setLabel('Treatment Type 2')
prop_type_TREATMENT_TYPE2.setManagedInternally(False)
prop_type_TREATMENT_TYPE2.setInternalNamespace(False)

prop_type_TREATMENT_TYPE2.setVocabulary(vocabulary_TREATMENT_TYPE)

prop_type_TREATMENT_TYPE3 = tr.createNewPropertyType('TREATMENT_TYPE3', DataType.CONTROLLEDVOCABULARY)
prop_type_TREATMENT_TYPE3.setLabel('Treatment Type 3')
prop_type_TREATMENT_TYPE3.setManagedInternally(False)
prop_type_TREATMENT_TYPE3.setInternalNamespace(False)

prop_type_TREATMENT_TYPE3.setVocabulary(vocabulary_TREATMENT_TYPE)


