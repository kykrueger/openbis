import openbis from '@src/js/services/openbis.js'

class TypeFormUtil {
  addTypePrefix(typeCode, propertyCode) {
    if (propertyCode && !propertyCode.startsWith(typeCode + '.')) {
      return typeCode + '.' + propertyCode
    } else {
      return propertyCode
    }
  }
  getDataTypeLabel(dataType) {
    if (dataType === openbis.DataType.SAMPLE) {
      return 'OBJECT'
    } else {
      return dataType
    }
  }
}

export default new TypeFormUtil()
