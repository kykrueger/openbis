from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel
import re

# A Regex for matching the column headers
header_regex = re.compile("^(.+)-(.*) ([0-9]+)")

def set_data_type(data_set):
  data_set.setPropertyValue("DATA_TYPE", "TRANSCRIPTOMICS")

def getInitialDataRowAndCol(metadata):
  """Extract the initial row and column as specified in the metadata. Returns an array with [row, col]."""
  # get the raw value from the map
  first_data_row = metadata.get("START DATA ROW")
  first_data_col = metadata.get("START DATA COL")

  # convert the row numeric string to an int
  if first_data_row is None:
    first_data_row = 0
  else:
    first_data_row = int(float(first_data_row)) - 1

  # convert the column spreadsheet value to an int
  if first_data_col is None:
    first_data_col = 0
  else:
    # columns start at A
    first_data_col = ord(first_data_col) - ord('A')
  return [first_data_row, first_data_col]


def retrieve_experiment(tr, exp_id):
  """Get the specified experiment form the server. Return the experiment."""
  if exp_id is None:
    exp = None
  else:
    exp = tr.getExperiment(exp_id)
  return exp

def strain_canonical(strainId):
  """Return the canonical form of the strainId"""
  if strainId.lower().startswith('jjs-din'):
    return "JJS-DIn" + strainId[7:]
  else:
    return strainId.upper()

def assign_properties(dataset, metadata):
  """Assign properties to the data set from information in the data."""
  propertyNameMap = {
    "STRAIN_NAMES": "STRAIN_NAMES",
    "TIMEPOINT TYPE": "TIMEPOINT_TYPE", 
    "CELL LOCATION": "CELL_LOCATION", 
    "VALUE TYPE": "VALUE_TYPE", 
    "VALUE UNIT": "VALUE_UNIT", 
    "SCALE": "SCALE"
    }
    
  for prop in metadata.keySet():
    key = propertyNameMap.get(prop)
    if key is not None:
      value = metadata.get(prop)
      if (key == "STRAIN"):
        value = value + " (STRAIN)"
      dataset.setPropertyValue(key, strain_canonical(value))
      
def convert_data_to_tsv(tr, start_row, start_col, dataset, location):
  """Create a tsv file containing the data and add it to the data set."""
  tr.createNewDirectory(dataset, location)
  tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + ".tsv")
  tsv = open(tsvFileName, 'w')
  raw_data = timeSeriesData.getRawDataLines()
  for i in range(start_row, len(raw_data)):
    line = raw_data[i]
    # write the metabolite id
    tsv.write(line[0])
    tsv.write("\t")
    for j in range(start_col, len(line) - 1):
      tsv.write(line[j])
      tsv.write("\t")
    tsv.write(line[len(line) - 1])
    tsv.write("\n")
  tsv.close()
  
class SplitColumnInfo:
  """
    A class that stores, for each column in the file, the column number, the strain name,
    the biological replicate, the hybridization number, and the column offset in the resulting file
  """
  def __init__(self, column, strain_name, bio_replicate, hybrid_number, output_col):
    self.column = column
    self.strain_name = strain_name
    self.bio_replicate = bio_replicate
    self.hybrid_number = hybrid_number
    self.output_col = output_col
    
  tsv = None
    
  
def convert_data_to_split_tsv(tr, start_row, start_col, dataset, location):
  """Create one tsv file per strain in the original data."""
  raw_data = timeSeriesData.getRawDataLines()
  
  # Keep track of the mapping from columns to strains and strains to columns
  column_infos = []
  strain_column_info = {}
  
  # Extract the column / strain mapping
  header_line = raw_data[start_row]
  for i in range(start_col, len(header_line)):
    match = header_regex.match(header_line[i])
    strain_name = match.group(1)
    strain_cols = strain_column_info.setdefault(strain_name, [])
    column_info = SplitColumnInfo(i, strain_name, match.group(2), match.group(3), len(strain_cols))
    strain_cols.append(column_info)
    column_infos.append(column_info)
    
  # create the files
  tr.createNewDirectory(dataset, location)
  for strain in strain_column_info.iterkeys():
    tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + "_" + strain + ".tsv")
    tsv = open(tsvFileName, 'w')
    for column_info in strain_column_info[strain]:
      column_info.tsv = tsv
      
  # Write the header
  line = raw_data[start_row]
  tag = line[0]
  # write the first column to each file
  for strain in strain_column_info.iterkeys():
    strain_column_info[strain][0].tsv.write(tag)
    
  for column_info in column_infos:
    column_info.tsv.write('\t')
    column_info.tsv.write(column_info.bio_replicate)
    column_info.tsv.write(' ')
    column_info.tsv.write(column_info.hybrid_number)

  # Write the data to the files
  for i in range(start_row + 1, len(raw_data)):
    line = raw_data[i]
    tag = line[0]
    for strain in strain_column_info.iterkeys():
      strain_column_info[strain][0].tsv.write('\n')
      # write the first column to each file
      strain_column_info[strain][0].tsv.write(tag)
    # Write the remaining data to each file
    for column_info in column_infos:
      column_info.tsv.write('\t')
      column_info.tsv.write(line[column_info.column])

  # Close each file
  for strain in strain_column_info.iterkeys():
    strain_column_info[strain][0].tsv.close()
  
def store_original_data(tr, dataset, location):
  """Put the original data into the data set."""
  tr.createNewDirectory(dataset, location)
  tr.moveFile(incoming.getAbsolutePath(), dataset, location + "/" + incoming.getName())

def extract_strains(start_row, start_col):
  """Extract the strain names from the header. These have already been validated by the validator."""
  strains = []
  line = timeSeriesData.getRawDataLines()[start_row]
  for i in range(start_col, len(line)):
    match = header_regex.match(line[i])
    strains.append(match.group(1))
  return ",".join(strains)

    

tr = service.transaction(incoming)
timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(incoming.getAbsolutePath())
dataStart = getInitialDataRowAndCol(timeSeriesData.getMetadataMap())

# create the data set and assign the metadata from the file
dataset = tr.createNewDataSet("TRANSCRIPTOMICS")
metadata = timeSeriesData.getMetadataMap()
metadata["STRAIN_NAMES"] = extract_strains(dataStart[0], dataStart[1])
assign_properties(dataset, metadata)

# Store the original and tsv data in data sets
original_dataset = tr.createNewDataSet("EXCEL_ORIGINAL")
set_data_type(original_dataset)
store_original_data(tr, original_dataset, "xls")

tsv_dataset = tr.createNewDataSet("TSV_MULTISTRAIN_EXPORT")
set_data_type(tsv_dataset)
convert_data_to_tsv(tr, dataStart[0], dataStart[1], tsv_dataset, "tsv-multi")

tsv_split_dataset = tr.createNewDataSet("TSV_EXPORT")
set_data_type(tsv_split_dataset)
convert_data_to_split_tsv(tr, dataStart[0], dataStart[1], tsv_split_dataset, "tsv")

# Make the original contain these
contained_codes = [original_dataset.getDataSetCode(), tsv_dataset.getDataSetCode(), tsv_split_dataset.getDataSetCode()]
dataset.setContainedDataSetCodes(contained_codes)


# If no experiment has been set, then get the experiment from the excel file
if dataset.getExperiment() is None:
  exp_id = metadata.get("EXPERIMENT")
  exp = retrieve_experiment(tr, exp_id)
  if exp is not None:
    dataset.setExperiment(exp)
    original_dataset.setExperiment(exp)
    tsv_dataset.setExperiment(exp)
    tsv_split_dataset.setExperiment(exp)

