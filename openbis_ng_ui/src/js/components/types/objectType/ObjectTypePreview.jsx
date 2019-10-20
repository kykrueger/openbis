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
    alignContent: 'flex-start',
    padding: theme.spacing(2),
    border: '1px solid green'
  }
})

class ObjectTypePreview extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.handleDragStart = this.handleDragStart.bind(this)
    this.handleDragEnd = this.handleDragEnd.bind(this)
    this.handleClick = this.handleClick.bind(this)
  }

  handleDragStart(start) {
    this.setState({
      currentDraggableId: start.draggableId
    })
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

    const { classes, type } = this.props

    return (
      <div className={classes.container} onClick={this.handleClick}>
        <Typography variant='h6'>Form Preview</Typography>
        <ObjectTypePreviewCode type={type} />
        <DragDropContext
          onDragStart={this.handleDragStart}
          onDragEnd={this.handleDragEnd}
        >
          <Droppable droppableId='root'>
            {provided => (
              <div
                ref={provided.innerRef}
                {...provided.droppableProps}
                className={classes.droppable}
              >
                {this.renderSectionsAndProperties()}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </DragDropContext>
      </div>
    )
  }

  renderSectionsAndProperties() {
    const { sections, properties } = this.props

    const elements = []
    let index = 0

    while (index < properties.length) {
      let property = properties[index]

      if (property.section) {
        let section = _.find(sections, ['id', property.section])
        elements.push(this.renderSection(section, index))
        index += section.properties.length
      } else {
        elements.push(this.renderProperty(property, index))
        index++
      }
    }

    return elements
  }

  renderSection(section, index) {
    const { properties, selection, onSelectionChange } = this.props
    const { currentDraggableId } = this.state

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
        isDroppable={
          currentDraggableId && currentDraggableId.startsWith('property-')
        }
      >
        {sectionProperties.map((sectionProperty, index) =>
          this.renderProperty(sectionProperty, index)
        )}
      </ObjectTypePreviewSection>
    )
  }

  renderProperty(property, index) {
    const { selection, onSelectionChange } = this.props

    return (
      <ObjectTypePreviewProperty
        key={property.id}
        property={property}
        index={index}
        selection={selection}
        onSelectionChange={onSelectionChange}
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreview)
