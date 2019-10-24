export function classNames(...classNames) {
  return classNames.filter(className => className).join(' ')
}
