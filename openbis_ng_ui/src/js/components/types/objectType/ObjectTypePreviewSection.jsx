import _ from 'lodash'
import React from 'react'
import { Draggable, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

import ObjectTypeHeader from './ObjectTypeHeader.jsx'

const styles = theme => ({
  draggable: {
    width: '100%',
    marginBottom: theme.spacing(2),
    '&:hover $droppable': {
      borderColor: theme.palette.background.secondary
    }
  },
  droppable: {
    padding: theme.spacing(2),
    borderWidth: '2px',
    borderStyle: 'dashed',
    borderColor: theme.palette.background.primary,
    backgroundColor: theme.palette.background.paper
  },
  named: {
    '& $droppable': {
      borderStyle: 'solid'
    }
  },
  selected: {
    '& $droppable': {
      borderColor: theme.palette.secondary.main
    },
    '&:hover $droppable': {
      borderColor: theme.palette.secondary.main
    }
  }
})

class ObjectTypePreviewSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    event.stopPropagation()
    this.props.onSelectionChange('section', {
      id: this.props.section.id,
      part: 'name'
    })
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
              classes.draggable,
              name ? classes.named : null,
              selected ? classes.selected : null
            )}
            onClick={this.handleClick}
          >
            <Droppable droppableId={id} type='property'>
              {provided => (
                <div>
                  <ObjectTypeHeader>{name}</ObjectTypeHeader>
                  <div
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className={classes.droppable}
                  >
                    <div>{children}</div>
                    {provided.placeholder}
                  </div>
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
