export const ERROR = { name: 'ERROR', importance: 4 }
export const INFO = { name: 'INFO', importance: 3 }
export const DEBUG = { name: 'DEBUG', importance: 2 }
export const TRACE = { name: 'TRACE', importance: 1 }

let currentLevel = INFO

let isLevelEnabled = level => {
  return level.importance >= currentLevel.importance
}

let log = (level, message, ...params) => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.log(message, ...params)
  }
}

let group = (level, message, ...params) => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.group(message, ...params)
  }
}

let groupEnd = level => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.groupEnd()
  }
}

export default {
  ERROR,
  INFO,
  DEBUG,
  TRACE,
  log,
  group,
  groupEnd,
  isLevelEnabled
}
