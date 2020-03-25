import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Browser from '@src/js/components/common/browser2/Browser.jsx'
import Content from '@src/js/components/common/content/Content.jsx'

import ObjectType from './objectType/ObjectType.jsx'
import CollectionType from './collectionType/CollectionType.jsx'
import DataSetType from './dataSetType/DataSetType.jsx'
import MaterialType from './materialType/MaterialType.jsx'
import Search from './search/Search.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

const objectTypeToComponent = {
  [objectType.OBJECT_TYPE]: ObjectType,
  [objectType.COLLECTION_TYPE]: CollectionType,
  [objectType.DATA_SET_TYPE]: DataSetType,
  [objectType.MATERIAL_TYPE]: MaterialType,
  [objectType.SEARCH]: Search
}

class Types extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Types.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <Browser page={pages.TYPES} />
        <Content
          page={pages.TYPES}
          objectTypeToComponent={objectTypeToComponent}
        />
      </div>
    )
  }
}

export default withStyles(styles)(Types)
