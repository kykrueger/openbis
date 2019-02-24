// TODO split initialstate when it becomes too big
export default {

  sessionActive: false,
  mode: 'TYPES',

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
