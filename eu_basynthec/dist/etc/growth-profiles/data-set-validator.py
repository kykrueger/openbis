def validate_data(timeSeriesData, errors):
	dataLines = timeSeriesData.getRawDataLines()
	lineCount = 0
	for line in dataLines:
		# The header needs to be Abs
		if lineCount is 0:
			if line[0] != "Strain":
				errors.append(createFileValidationError("The first data column must be 'Strain'"))
				break
			lineCount = lineCount + 1
			continue

		# The compound id should be one of these forms
		strain = line[0]
		if not isStrainIdValid(strain):
			errors.append(createFileValidationError("Line " + str(lineCount + 1) + ", column 1 must be MGP[0-999] (instead of " + strain + ")."))
		lineCount = lineCount + 1

def validate_metadata(time_series_data, errors):
	metadata = time_series_data.getMetadataMap()
	validationHelper = ValidationHelper(metadata, errors)
	
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
		if metadata.get("VALUE UNIT").lower() != 'dimensionless':
			errors.append(createFileValidationError("The value unit must be 'DIMENSIONLESS'"))
	
	# validate the value type
	if validationHelper.checkIsSpecified("SCALE", "scale"):
		if metadata.get("SCALE").lower() not in ['lin', 'log2', 'log10', 'ln']:
			errors.append(createFileValidationError("The scale must be one of 'lin', 'log2', 'log10', 'ln'"))

def validate_data_set_file(file):
	errors = []
	time_series_data = create_time_series_excel(file.getAbsolutePath())
	if time_series_data is None:
		errors.append(createFileValidationError(file.getName() + " is not an Excel file."))
		return errors
		
	# validate the metadata
	validate_metadata(time_series_data, errors)
			
	# validate the data
	validate_data(time_series_data, errors)
	
	return errors
