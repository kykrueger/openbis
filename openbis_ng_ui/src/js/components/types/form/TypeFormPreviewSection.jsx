import _ from 'lodash'
import React from 'react'
import Typography from '@material-ui/core/Typography'
import { Draggable, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const styles = theme => ({
  draggable: {
    width: '100%',
    cursor: 'pointer',
    marginBottom: theme.spacing(1),
    '&:hover $droppable': {
      borderColor: theme.palette.border.primary
    }
  },
  droppable: {
    padding: theme.spacing(1),
    borderWidth: '2px',
    borderStyle: 'dashed',
    borderColor: theme.palette.border.secondary,
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

class TypeFormPreviewSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleClick = this.handleClick.bind(this)
  }

  handleClick(event) {
    const { selection } = this.props

    event.stopPropagation()

    const newSelection = {
      type: 'section',
      params: {
        id: this.props.section.id,
        part: 'name'
      }
    }

    if (_.isEqual(selection, newSelection)) {
      this.props.onSelectionChange()
    } else {
      this.props.onSelectionChange(newSelection.type, newSelection.params)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormPreviewSection.render')

    let { mode, section, index, children, selection, classes } = this.props
    let { id, name } = section

    const selected =
      selection &&
      selection.type === 'section' &&
      selection.params.id === section.id

    return (
      <Draggable
        draggableId={id}
        index={index}
        isDragDisabled={mode !== 'edit'}
      >
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            {...provided.dragHandleProps}
            className={util.classNames(
              classes.draggable,
              name.value ? classes.named : null,
              selected ? classes.selected : null
            )}
            onClick={this.handleClick}
          >
            <Droppable droppableId={id} type='property'>
              {provided => (
                <div>
                  <Typography variant='body2' data-part='name'>
                    {name.value}
                  </Typography>
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

export default _.flow(withStyles(styles))(TypeFormPreviewSection)
