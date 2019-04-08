import _ from 'lodash'
import React from 'react'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import logger from '../../common/logger.js'

class ContentTabs extends React.Component {

  handleTabChange = (event, value) => {
    let object = this.props.objects[value]
    this.props.objectSelect(object.type, object.id)
  }

  handleTabClose = (event, object) => {
    this.props.objectClose(object.type, object.id)
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'ContentTabs.render')

    return (
      <Tabs
        value={_.findIndex(this.props.objects, this.props.selectedObject)}
        onChange={this.handleTabChange}
      >
        {this.props.objects.map(object =>
          <Tab key={`${object.type}/${object.id}`}
            label={object.id}
            icon={<CloseIcon onClick={(event) => this.handleTabClose(event, object)}/>}
          />
        )}
      </Tabs>
    )
  }

}

export default ContentTabs
