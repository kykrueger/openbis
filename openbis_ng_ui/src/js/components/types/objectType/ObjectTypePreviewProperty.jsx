import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypePreviewPropertyVarchar from './ObjectTypePreviewPropertyVarchar.jsx'
import ObjectTypePreviewPropertyNumber from './ObjectTypePreviewPropertyNumber.jsx'
import ObjectTypePreviewPropertyBoolean from './ObjectTypePreviewPropertyBoolean.jsx'
import ObjectTypePreviewPropertyVocabulary from './ObjectTypePreviewPropertyVocabulary.jsx'
import ObjectTypePreviewPropertyMaterial from './ObjectTypePreviewPropertyMaterial.jsx'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const styles = () => ({
  container: {
    padding: '10px',
    border: '1px solid white',
    '&:hover': {
      border: '1px solid blue'
    }
  },
  selected: {}
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
            {this.renderProperty(property)}
          </div>
        )}
      </Draggable>
    )
  }

  renderProperty(property) {
    const dataType = property.dataType

    if (dataType === 'VARCHAR' || dataType === 'MULTILINE_VARCHAR') {
      return <ObjectTypePreviewPropertyVarchar property={property} />
    } else if (dataType === 'REAL' || dataType === 'INTEGER') {
      return <ObjectTypePreviewPropertyNumber property={property} />
    } else if (dataType === 'BOOLEAN') {
      return <ObjectTypePreviewPropertyBoolean property={property} />
    } else if (dataType === 'CONTROLLEDVOCABULARY') {
      return <ObjectTypePreviewPropertyVocabulary property={property} />
    } else if (dataType === 'MATERIAL') {
      return <ObjectTypePreviewPropertyMaterial property={property} />
    } else {
      return <span>Data type not supported yet</span>
    }
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewProperty)
