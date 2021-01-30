class TypeFormUtil {
  addTypePrefix(typeCode, propertyCode) {
    if (propertyCode && !propertyCode.startsWith(typeCode + '.')) {
      return typeCode + '.' + propertyCode
    } else {
      return propertyCode
    }
  }
}

export default new TypeFormUtil()
