function classNames(...classNames) {
  return classNames.filter(className => className).join(' ')
}

function trim(str) {
  return str && str.trim().length > 0 ? str.trim() : null
}

export default {
  classNames,
  trim
}
