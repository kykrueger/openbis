import _ from 'lodash'
import React from 'react'
import { Draggable, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const styles = () => ({
  container: {
    padding: '10px'
  },
  selected: {
    border: '1px solid red'
  }
})

class ObjectTypePreviewSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    event.stopPropagation()
    this.props.onSelectionChange('section', { id: this.props.section.id })
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewSection.render')

    let { section, index, children, selection, classes } = this.props
    let { id, name } = section

    const selected =
      selection &&
      selection.type === 'section' &&
      selection.params.id === section.id

    return (
      <Draggable draggableId={id} index={index}>
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
            <Droppable droppableId={id} type='property'>
              {provided => (
                <div ref={provided.innerRef} {...provided.droppableProps}>
                  Section {name}
                  <div>{children}</div>
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </div>
        )}
      </Draggable>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewSection)
