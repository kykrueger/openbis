import os
import re
from eu.basynthec.cisd.dss import TimeSeriesDataExcel, ValidationHelper

def validate_data(timeSeriesData, errors):
	chebiRegex = re.compile("^CHEBI:.*")
	bsbmeRegex = re.compile("^BSBME:.*")
	dataLines = timeSeriesData.getRawDataLines()
	lineCount = 0
	for line in dataLines:
		# The header needs to be CompoundID
		if lineCount is 0:
			if line[0] != "CompoundID":
				errors.append(createFileValidationError("The first data column must be 'CompoundID'"))
				break
			lineCount = lineCount + 1
			continue

		# The compound id should be one of these forms
		compoundId = line[0]
		if not chebiRegex.match(compoundId):
			if not bsbmeRegex.match(compoundId):
				errors.append(createFileValidationError("Line " + str(lineCount + 1) + " must be of the format 'CHEBI:#' or 'BSBME:#'"))
		lineCount = lineCount + 1

def validate_data_set_file(file):
	errors = []
	timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(file.getAbsolutePath())
	if timeSeriesData is None:
		errors.append(createFileValidationError(file.getName() + " is not an Excel file."))
		return errors
		
	metadata = timeSeriesData.getMetadataMap()
	validationHelper = ValidationHelper(metadata, errors)
	
	# validate the strain
	validationHelper.checkIsSpecified("STRAIN", "strain")
	
	# validate the timepoint type
	if validationHelper.checkIsSpecified("TIMEPOINT TYPE", "time point type"):
		if metadata.get("TIMEPOINT TYPE").upper() not in ['EX', 'IN', 'SI']:
			errors.append(createFileValidationError("The timepoint type must be one of 'EX', 'IN', 'SI'"))
			
	# validate the cell location
	if validationHelper.checkIsSpecified("CELL LOCATION", "cell location"):
		if metadata.get("CELL LOCATION").upper() not in ['CE', 'ES', 'ME', 'CY', 'NC']:
			errors.append(createFileValidationError("The cell location must be one of 'CE', 'ES', 'ME', 'CY', 'NC'"))
		
	# validate the value type
	if validationHelper.checkIsSpecified("VALUE TYPE", "value type"):
		if metadata.get("VALUE TYPE").lower() not in ['value', 'mean', 'median', 'std', 'var', 'error', 'iqr']:
			errors.append(createFileValidationError("The value type must be one of 'Value', 'Mean', 'Median', 'Std', 'Var', 'Error', 'Iqr'"))

	# validate the value unit
	if validationHelper.checkIsSpecified("VALUE UNIT", "value unit"):
		if metadata.get("VALUE UNIT").lower() not in ['mm', 'um', 'ratiot1', 'ratiocs']:
			errors.append(createFileValidationError("The value unit must be one of 'mM', 'uM', 'RatioT1', 'RatioCs'"))
	
	# validate the value type
	if validationHelper.checkIsSpecified("SCALE", "scale"):
		if metadata.get("SCALE").lower() not in ['lin', 'log2', 'log10', 'ln']:
			errors.append(createFileValidationError("The scale must be one of 'lin', 'log2', 'log10', 'ln'"))
			
	# validate the data
	validate_data(timeSeriesData, errors)
	
	return errors
