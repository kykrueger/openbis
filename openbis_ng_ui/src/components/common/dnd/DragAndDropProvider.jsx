import React from 'react'
import { DragDropContextProvider } from 'react-dnd'
import HTML5Backend from 'react-dnd-html5-backend'
import logger from '../../../common/logger.js'

class DragAndDropProvider extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'DragAndDropProvider.render')

    return (
      <DragDropContextProvider backend={HTML5Backend}>
        {this.props.children}
      </DragDropContextProvider>
    )
  }
}

export default DragAndDropProvider
