import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Content from '@src/js/components/common/content/Content.jsx'
import ContentNewObjectTab from '@src/js/components/common/content/ContentNewObjectTab.jsx'
import ContentObjectTab from '@src/js/components/common/content/ContentObjectTab.jsx'
import ContentSearchTab from '@src/js/components/common/content/ContentSearchTab.jsx'

import TypesBrowser from './browser/TypesBrowser.jsx'
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

class Types extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Types.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <TypesBrowser />
        <Content
          page={pages.TYPES}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(tab) {
    const { object } = tab
    if (
      object.type === objectType.OBJECT_TYPE ||
      object.type === objectType.NEW_OBJECT_TYPE
    ) {
      return <ObjectType object={object} />
    } else if (object.type === objectType.COLLECTION_TYPE) {
      return <CollectionType objectId={object.id} />
    } else if (object.type === objectType.DATA_SET_TYPE) {
      return <DataSetType objectId={object.id} />
    } else if (object.type === objectType.MATERIAL_TYPE) {
      return <MaterialType objectId={object.id} />
    } else if (object.type === objectType.SEARCH) {
      return <Search objectId={object.id} />
    }
  }

  renderTab(tab) {
    const { object, changed } = tab
    if (
      object.type === objectType.OBJECT_TYPE ||
      object.type === objectType.COLLECTION_TYPE ||
      object.type === objectType.DATA_SET_TYPE ||
      object.type === objectType.MATERIAL_TYPE
    ) {
      return <ContentObjectTab object={object} changed={changed} />
    } else if (object.type === objectType.NEW_OBJECT_TYPE) {
      return <ContentNewObjectTab name='New Object Type' object={object} />
    } else if (object.type === objectType.NEW_COLLECTION_TYPE) {
      return <ContentNewObjectTab name='New Collection Type' object={object} />
    } else if (object.type === objectType.NEW_DATA_SET_TYPE) {
      return <ContentNewObjectTab name='New Data Set Type' object={object} />
    } else if (object.type === objectType.NEW_MATERIAL_TYPE) {
      return <ContentNewObjectTab name='New Material Type' object={object} />
    } else if (object.type === objectType.SEARCH) {
      return <ContentSearchTab object={object} />
    }
  }
}

export default withStyles(styles)(Types)
