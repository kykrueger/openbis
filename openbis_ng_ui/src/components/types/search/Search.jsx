import React from 'react'
import {facade, dto} from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

class Search extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
    }
  }

  componentDidMount(){
    this.load()
  }

  load(){
    this.setState({
      loaded: false
    })

    Promise.all([
      this.searchObjectTypes(),
      this.searchCollectionTypes(),
      this.searchDataSetTypes(),
      this.searchMaterialTypes()
    ]).then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
      this.setState(() => ({
        loaded: true,
        types: [].concat(objectTypes, collectionTypes, dataSetTypes, materialTypes)
      }))
    })
  }

  searchObjectTypes(){
    let criteria = new dto.SampleTypeSearchCriteria()
    let fo = new dto.SampleTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchSampleTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchCollectionTypes(){
    let criteria = new dto.ExperimentTypeSearchCriteria()
    let fo = new dto.ExperimentTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchExperimentTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchDataSetTypes(){
    let criteria = new dto.DataSetTypeSearchCriteria()
    let fo = new dto.DataSetTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchDataSetTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchMaterialTypes(){
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchMaterialTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if(!this.state.loaded){
      return <React.Fragment />
    }

    return this.state.types.map(type => {
      return <div key={type.permId}>{type.code}</div>
    })
  }

}

export default Search
