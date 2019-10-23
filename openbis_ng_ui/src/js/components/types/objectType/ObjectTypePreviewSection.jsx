import _ from 'lodash'
import React from 'react'
import { Draggable, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const styles = () => ({
  draggable: {
    marginBottom: '10px'
  },
  droppable: {
    padding: '10px',
    borderWidth: '1px',
    borderStyle: 'dashed',
    borderColor: 'lightgray',
    '&:hover': {
      borderColor: 'blue'
    }
  },
  named: {
    '& $droppable': {
      borderStyle: 'solid',
      borderColor: 'gray',
      '&:hover': {
        borderColor: 'blue'
      }
    }
  },
  selected: {
    '& $droppable': {
      borderColor: 'red',
      '&:hover': {
        borderColor: 'red'
      }
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
              classes.draggable,
              name ? classes.named : null,
              selected ? classes.selected : null
            )}
            onClick={this.handleClick}
          >
            <Droppable droppableId={id} type='property'>
              {provided => (
                <React.Fragment>
                  <Typography variant='h6'>{name}</Typography>
                  <div
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className={classes.droppable}
                  >
                    <div>{children}</div>
                    {provided.placeholder}
                  </div>
                </React.Fragment>
              )}
            </Droppable>
          </div>
        )}
      </Draggable>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewSection)
