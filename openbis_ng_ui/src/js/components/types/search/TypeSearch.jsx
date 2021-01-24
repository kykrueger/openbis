import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import TypesGrid from '@src/js/components/types/common/TypesGrid.jsx'
import VocabulariesGrid from '@src/js/components/types/common/VocabulariesGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class TypeSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {
      loaded: false,
      selection: null
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([
        this.loadObjectTypes(),
        this.loadCollectionTypes(),
        this.loadDataSetTypes(),
        this.loadMaterialTypes(),
        this.loadVocabularyTypes()
      ])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      store.dispatch(actions.errorChange(error))
    }
  }

  async loadObjectTypes() {
    if (!this.shouldLoad(objectTypes.OBJECT_TYPE)) {
      return
    }

    const fo = new openbis.SampleTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchSampleTypes(
      new openbis.SampleTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        listable: _.get(object, 'listable', false),
        subcodeUnique: _.get(object, 'subcodeUnique', false),
        autoGeneratedCode: _.get(object, 'autoGeneratedCode', false),
        showContainer: _.get(object, 'showContainer', false),
        showParents: _.get(object, 'showParents', false),
        showParentMetadata: _.get(object, 'showParentMetadata', false),
        generatedCodePrefix: _.get(object, 'generatedCodePrefix'),
        validationPlugin: _.get(object, 'validationPlugin.name')
      }))

    this.setState({
      objectTypes: types
    })
  }

  async loadCollectionTypes() {
    if (!this.shouldLoad(objectTypes.COLLECTION_TYPE)) {
      return
    }

    const fo = new openbis.ExperimentTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchExperimentTypes(
      new openbis.ExperimentTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        validationPlugin: _.get(object, 'validationPlugin.name')
      }))

    this.setState({
      collectionTypes: types
    })
  }

  async loadDataSetTypes() {
    if (!this.shouldLoad(objectTypes.DATA_SET_TYPE)) {
      return
    }

    const fo = new openbis.DataSetTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchDataSetTypes(
      new openbis.DataSetTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        validationPlugin: _.get(object, 'validationPlugin.name'),
        mainDataSetPattern: _.get(object, 'mainDataSetPattern'),
        mainDataSetPath: _.get(object, 'mainDataSetPath'),
        disallowDeletion: _.get(object, 'disallowDeletion', false)
      }))

    this.setState({
      dataSetTypes: types
    })
  }

  async loadMaterialTypes() {
    if (!this.shouldLoad(objectTypes.MATERIAL_TYPE)) {
      return
    }

    const fo = new openbis.MaterialTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchMaterialTypes(
      new openbis.MaterialTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        validationPlugin: _.get(object, 'validationPlugin.name')
      }))

    this.setState({
      materialTypes: types
    })
  }

  async loadVocabularyTypes() {
    if (!this.shouldLoad(objectTypes.VOCABULARY_TYPE)) {
      return
    }

    const result = await openbis.searchVocabularies(
      new openbis.VocabularySearchCriteria(),
      new openbis.VocabularyFetchOptions()
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: object.code,
        code: object.code,
        description: object.description,
        urlTemplate: object.urlTemplate
      }))

    this.setState({
      vocabularyTypes: types
    })
  }

  shouldLoad(objectType) {
    return this.props.objectType === objectType || !this.props.objectType
  }

  handleClickContainer() {
    this.setState({
      selection: null
    })
  }

  handleSelectedRowChange(objectType) {
    return row => {
      if (row) {
        this.setState({
          selection: {
            type: objectType,
            id: row.id
          }
        })
      }
    }
  }

  getSelectedRowId(objectType) {
    const { selection } = this.state
    return selection && selection.type === objectType ? selection.id : null
  }

  render() {
    logger.log(logger.DEBUG, 'TypeSearch.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer onClick={this.handleClickContainer}>
        {this.renderNoResultsFoundMessage()}
        {this.renderObjectTypes()}
        {this.renderCollectionTypes()}
        {this.renderDataSetTypes()}
        {this.renderMaterialTypes()}
        {this.renderVocabularyTypes()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const {
      objectTypes = [],
      collectionTypes = [],
      dataSetTypes = [],
      materialTypes = [],
      vocabularyTypes = []
    } = this.state

    if (
      !objectType &&
      objectTypes.length === 0 &&
      collectionTypes.length === 0 &&
      dataSetTypes.length === 0 &&
      materialTypes.length === 0 &&
      vocabularyTypes.length === 0
    ) {
      return (
        <Message type='info'>{messages.get(messages.NO_RESULTS_FOUND)}</Message>
      )
    } else {
      return null
    }
  }

  renderObjectTypes() {
    if (this.shouldRender(objectTypes.OBJECT_TYPE, this.state.objectTypes)) {
      return (
        <TypesGrid
          id={ids.OBJECT_TYPES_GRID_ID}
          kind={openbis.EntityKind.SAMPLE}
          rows={this.state.objectTypes}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.OBJECT_TYPE
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.OBJECT_TYPE)}
        />
      )
    } else {
      return null
    }
  }

  renderCollectionTypes() {
    if (
      this.shouldRender(objectTypes.COLLECTION_TYPE, this.state.collectionTypes)
    ) {
      return (
        <TypesGrid
          id={ids.COLLECTION_TYPES_GRID_ID}
          kind={openbis.EntityKind.EXPERIMENT}
          rows={this.state.collectionTypes}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.COLLECTION_TYPE
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.COLLECTION_TYPE)}
        />
      )
    } else {
      return null
    }
  }

  renderDataSetTypes() {
    if (this.shouldRender(objectTypes.DATA_SET_TYPE, this.state.dataSetTypes)) {
      return (
        <TypesGrid
          id={ids.DATA_SET_TYPES_GRID_ID}
          kind={openbis.EntityKind.DATA_SET}
          rows={this.state.dataSetTypes}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.DATA_SET_TYPE
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.DATA_SET_TYPE)}
        />
      )
    } else {
      return null
    }
  }

  renderMaterialTypes() {
    if (
      this.shouldRender(objectTypes.MATERIAL_TYPE, this.state.materialTypes)
    ) {
      return (
        <TypesGrid
          id={ids.MATERIAL_TYPES_GRID_ID}
          kind={openbis.EntityKind.MATERIAL}
          rows={this.state.materialTypes}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.MATERIAL_TYPE
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.MATERIAL_TYPE)}
        />
      )
    } else {
      return null
    }
  }

  renderVocabularyTypes() {
    if (
      this.shouldRender(objectTypes.VOCABULARY_TYPE, this.state.vocabularyTypes)
    ) {
      return (
        <VocabulariesGrid
          id={ids.VOCABULARY_TYPES_GRID_ID}
          rows={this.state.vocabularyTypes}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.VOCABULARY_TYPE
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.VOCABULARY_TYPE)}
        />
      )
    } else {
      return null
    }
  }

  shouldRender(objectType, types) {
    return this.props.objectType === objectType || (types && types.length > 0)
  }
}

export default TypeSearch
