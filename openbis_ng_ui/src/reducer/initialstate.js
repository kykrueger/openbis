function foldLeft(array, acc, reducer) {
  if (array.length === 0) {
    return acc
  } else {
    const [head, ...tail] = array
    return foldLeft(tail, reducer(acc, head), reducer)
  }
}

const take = (array, n) => array.slice(0, n)

//const stringColumns = ['Description', 'Color', 'Address', 'Details', 'Info', 'Additonals', 'Other', 'Note', 'Optional', 'Something']
//const intColumns = ['Weight', 'Length', 'Width', 'Number', 'Amount', 'Depth', 'Temperature', 'Volume', 'Height', 'Opacity']

const stringColumns = ['Description', 'Color', 'Address']
const intColumns = ['Weight', 'Length', 'Width']


function shuffle(arr) {
  return arr.slice(0).sort((a, b) => Math.random() * 3 - 1.5)
}

function randomString() {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

function createEntity(id) {
  const numStrings = Math.random() * 3
  const numInts = Math.random() * 3
  let entity = {Id: id}
  entity = foldLeft(take(shuffle(stringColumns), numStrings), entity, (acc, a) => Object.assign({}, acc, {[a]: randomString()}))
  entity = foldLeft(take(shuffle(intColumns), numInts), entity, (acc, a) => Object.assign({}, acc, {[a]: Math.floor(Math.random() * 100)}))
  return entity
}

const data = [...Array(10000).keys()].map(i => createEntity(i + 1))

let tableColumns = foldLeft(stringColumns, {}, (acc, a) => Object.assign({}, acc, {[a]: 'string'}))
tableColumns = foldLeft(intColumns, tableColumns, (acc, a) => Object.assign({}, acc, {[a]: 'int'}))
tableColumns = foldLeft(shuffle(Object.entries(tableColumns)), {Id: 'int'}, (acc, a) => Object.assign({}, acc, {[a[0]]: a[1]}))

// TODO split initialstate when it becomes too big
export default {

  sessionActive: false,
  mode: 'DATABASE',

  database: {
    spaces: {},
    projects: {},
    table: {
      initialData: data,
      data: data,
      columns: tableColumns,
      page: 0,
      sortColumn: '',
      sortDirection: 'asc'
    },
    browser: {
      loaded: false,
      filter: '',
      nodes: []
    },
    openEntities: {
      entities: [],
      selectedEntity: null,
    },
    dirtyEntities: []
  },

  types: {
    browser: {
      loaded: false,
      filter: '',
      nodes: []
    },
    openEntities: {
      entities: [],
      selectedEntity: null,
    }
  },

  users: {
    browser: {
      loaded: false,
      filter: '',
      nodes: []
    },
    openEntities: {
      entities: [],
      selectedEntity: null,
    }
  },

  favourites: {},
  tools: {},

  loading: false,
  exceptions: [],
}