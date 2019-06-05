import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../common/logger.js'
import * as pages from '../../common/consts/pages.js'
import * as objectType from '../../common/consts/objectType.js'

import Browser from '../common/browser/Browser.jsx'
import Content from '../common/content/Content.jsx'

import ObjectType from './objectType/ObjectType.jsx'
import CollectionType from './collectionType/CollectionType.jsx'
import DataSetType from './dataSetType/DataSetType.jsx'
import MaterialType from './materialType/MaterialType.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  },
})

const objectTypeToComponent = {
  [objectType.OBJECT_TYPE]: ObjectType,
  [objectType.COLLECTION_TYPE]: CollectionType,
  [objectType.DATA_SET_TYPE]: DataSetType,
  [objectType.MATERIAL_TYPE]: MaterialType
}

class Types extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Types.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <Browser page={pages.TYPES}/>
        <Content page={pages.TYPES} objectTypeToComponent={objectTypeToComponent}/>
      </div>
    )
  }

}

export default withStyles(styles)(Types)
