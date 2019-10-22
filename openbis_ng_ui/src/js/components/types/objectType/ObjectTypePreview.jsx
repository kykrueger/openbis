import _ from 'lodash'
import React from 'react'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import ObjectTypePreviewCode from './ObjectTypePreviewCode.jsx'
import ObjectTypePreviewProperty from './ObjectTypePreviewProperty.jsx'
import ObjectTypePreviewSection from './ObjectTypePreviewSection.jsx'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2),
    height: '100%',
    boxSizing: 'border-box'
  },
  droppable: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    alignContent: 'flex-start'
  }
})

class ObjectTypePreview extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleDragEnd = this.handleDragEnd.bind(this)
    this.handleClick = this.handleClick.bind(this)
  }

  handleDragEnd(result) {
    if (!result.destination) {
      return
    }

    if (result.type === 'section') {
      this.props.onOrderChange('section', {
        fromIndex: result.source.index,
        toIndex: result.destination.index
      })
    } else if (result.type === 'property') {
      this.props.onOrderChange('property', {
        fromSectionId: result.source.droppableId,
        fromIndex: result.source.index,
        toSectionId: result.destination.droppableId,
        toIndex: result.destination.index
      })
    }
  }

  handleClick() {
    this.props.onSelectionChange()
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreview.render')

    const { classes, type, sections } = this.props

    return (
      <div className={classes.container} onClick={this.handleClick}>
        <Typography variant='h6'>Form Preview</Typography>
        <ObjectTypePreviewCode type={type} />
        <DragDropContext onDragEnd={this.handleDragEnd}>
          <Droppable droppableId='root' type='section'>
            {provided => (
              <div
                ref={provided.innerRef}
                {...provided.droppableProps}
                className={classes.droppable}
              >
                {sections.map((section, index) =>
                  this.renderSection(section, index)
                )}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </DragDropContext>
      </div>
    )
  }

  renderSection(section, index) {
    const { properties, selection, onSelectionChange } = this.props

    const sectionProperties = section.properties.map(id =>
      _.find(properties, ['id', id])
    )

    return (
      <ObjectTypePreviewSection
        key={section.id}
        section={section}
        index={index}
        selection={selection}
        onSelectionChange={onSelectionChange}
      >
        {this.renderProperties(sectionProperties, 0)}
      </ObjectTypePreviewSection>
    )
  }

  renderProperties(properties, index) {
    const { selection, onSelectionChange } = this.props

    return properties.map((property, offset) => {
      return (
        <ObjectTypePreviewProperty
          key={property.id}
          property={property}
          index={index + offset}
          selection={selection}
          onSelectionChange={onSelectionChange}
        />
      )
    })
  }
}

export default withStyles(styles)(ObjectTypePreview)
