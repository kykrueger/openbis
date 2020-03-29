import _ from 'lodash'
import React from 'react'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

import ObjectTypePreviewHeader from './ObjectTypePreviewHeader.jsx'
import ObjectTypePreviewProperty from './ObjectTypePreviewProperty.jsx'
import ObjectTypePreviewSection from './ObjectTypePreviewSection.jsx'

const styles = theme => ({
  container: {
    flex: '1 1 auto',
    display: 'flex',
    padding: `${theme.spacing(2)}px ${theme.spacing(4)}px`
  },
  form: {},
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
    this.state = {}
    this.handleDragStart = this.handleDragStart.bind(this)
    this.handleDragEnd = this.handleDragEnd.bind(this)
    this.handleClick = this.handleClick.bind(this)
  }

  handleDragStart(start) {
    this.setState({ dragging: true })

    this.props.onSelectionChange(start.type, {
      id: start.draggableId
    })
  }

  handleDragEnd(result) {
    this.setState({ dragging: false })

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
    if (!this.state.dragging) {
      this.props.onSelectionChange()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreview.render')

    const { classes, type, sections } = this.props

    return (
      <div className={classes.container} onClick={this.handleClick}>
        <div className={classes.form}>
          <ObjectTypePreviewHeader type={type} />
          <DragDropContext
            onDragStart={this.handleDragStart}
            onDragEnd={this.handleDragEnd}
          >
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
    const { controller, selection, onSelectionChange } = this.props

    return properties.map((property, offset) => {
      return (
        <ObjectTypePreviewProperty
          key={property.id}
          controller={controller}
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
