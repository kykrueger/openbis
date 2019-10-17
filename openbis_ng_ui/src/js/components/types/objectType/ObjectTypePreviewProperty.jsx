import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const styles = () => ({
  container: {
    padding: '10px'
  },
  selected: {
    border: '1px solid black'
  }
})

class ObjectTypePreviewProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    event.stopPropagation()
    this.props.onSelectionChange('property', { id: this.props.property.id })
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewProperty.render')

    let { property, index, selection, classes } = this.props

    const selected =
      selection &&
      selection.type === 'property' &&
      selection.params.id === property.id

    return (
      <Draggable draggableId={property.id} index={index}>
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            {...provided.dragHandleProps}
            className={util.classNames(
              classes.container,
              selected ? classes.selected : null
            )}
            onClick={this.handleClick}
          >
            {property.code}
          </div>
        )}
      </Draggable>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewProperty)
