import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import SelectField from '../../common/form/SelectField.jsx'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const EMPTY = 'empty'

const styles = theme => ({
  draggable: {
    width: '100%',
    padding: theme.spacing(2),
    boxSizing: 'border-box',
    borderWidth: '2px',
    borderStyle: 'solid',
    borderColor: theme.palette.background.paper,
    backgroundColor: theme.palette.background.paper,
    '&:last-child': {
      marginBottom: 0
    },
    '&:hover': {
      borderColor: theme.palette.background.secondary
    }
  },
  selected: {
    borderColor: theme.palette.secondary.main,
    '&:hover': {
      borderColor: theme.palette.secondary.main
    }
  },
  hidden: {
    opacity: 0.4
  },
  partEmpty: {
    fontStyle: 'italic',
    opacity: 0.7,
    color: theme.palette.grey.main
  },
  partSelected: {
    cursor: 'pointer',
    pointerEvents: 'initial',
    paddingBottom: '1px',
    borderColor: theme.palette.secondary.main,
    borderStyle: 'solid',
    borderWidth: '0px 0px 2px 0px',
    '&:hover': {
      borderColor: theme.palette.secondary.main
    }
  },
  partNotSelected: {
    cursor: 'pointer',
    pointerEvents: 'initial',
    paddingBottom: '1px',
    '&:hover': {
      borderStyle: 'solid',
      borderWidth: '0px 0px 2px 0px',
      borderColor: theme.palette.background.secondary
    }
  }
})

class ObjectTypePreviewProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.handleDraggableClick = this.handleDraggableClick.bind(this)
    this.handlePropertyClick = this.handlePropertyClick.bind(this)
  }

  componentDidMount() {
    const { dataType } = this.props.property

    if (dataType === 'MATERIAL') {
      this.loadMaterialProperty()
    } else if (dataType === 'CONTROLLEDVOCABULARY') {
      this.loadVocabularyProperty()
    }
  }

  componentDidUpdate(prevProps) {
    let { property: prevProperty } = prevProps
    let { property } = this.props

    if (property.materialType !== prevProperty.materialType) {
      this.loadMaterialProperty()
    } else if (property.vocabulary !== prevProperty.vocabulary) {
      this.loadVocabularyProperty()
    }
  }

  loadMaterialProperty() {
    const materialType = this.props.property.materialType

    if (materialType) {
      let criteria = new dto.MaterialSearchCriteria()
      let fo = new dto.MaterialFetchOptions()

      criteria
        .withType()
        .withCode()
        .thatEquals(materialType)

      return facade.searchMaterials(criteria, fo).then(result => {
        this.setState(() => ({
          materials: result.objects
        }))
      })
    } else {
      this.setState(() => ({
        materials: null
      }))
    }
  }

  loadVocabularyProperty() {
    const vocabulary = this.props.property.vocabulary

    if (vocabulary) {
      let criteria = new dto.VocabularyTermSearchCriteria()
      let fo = new dto.VocabularyTermFetchOptions()

      criteria
        .withVocabulary()
        .withCode()
        .thatEquals(vocabulary)

      return facade.searchVocabularyTerms(criteria, fo).then(result => {
        this.setState(() => ({
          terms: result.objects
        }))
      })
    } else {
      this.setState(() => ({
        terms: null
      }))
    }
  }

  handleDraggableClick(event) {
    event.stopPropagation()
    this.props.onSelectionChange('property', {
      id: this.props.property.id
    })
  }

  handlePropertyClick(event) {
    event.stopPropagation()
    this.props.onSelectionChange('property', {
      id: this.props.property.id,
      part: event.target.dataset.part
    })
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewProperty.render')

    let { property, selection, index, classes } = this.props

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
              classes.draggable,
              selected ? classes.selected : null
            )}
            onClick={this.handleDraggableClick}
          >
            {this.renderProperty()}
          </div>
        )}
      </Draggable>
    )
  }

  renderProperty() {
    const { dataType } = this.props.property

    if (dataType === 'VARCHAR' || dataType === 'MULTILINE_VARCHAR') {
      return this.renderVarcharProperty()
    } else if (dataType === 'REAL' || dataType === 'INTEGER') {
      return this.renderNumberProperty()
    } else if (dataType === 'BOOLEAN') {
      return this.renderBooleanProperty()
    } else if (dataType === 'CONTROLLEDVOCABULARY') {
      return this.renderVocabularyProperty()
    } else if (dataType === 'MATERIAL') {
      return this.renderMaterialProperty()
    } else {
      return <span>Data type not supported yet</span>
    }
  }

  renderVarcharProperty() {
    return (
      <TextField
        label={this.getLabel()}
        description={this.getDescription()}
        mandatory={this.getMandatory()}
        metadata={this.getMetadata()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
      />
    )
  }

  renderNumberProperty() {
    return (
      <TextField
        type='number'
        label={this.getLabel()}
        description={this.getDescription()}
        mandatory={this.getMandatory()}
        metadata={this.getMetadata()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
      />
    )
  }

  renderBooleanProperty() {
    return (
      <div>
        <CheckboxField
          label={this.getLabel()}
          description={this.getDescription()}
          mandatory={this.getMandatory()}
          metadata={this.getMetadata()}
          styles={this.getStyles()}
          onClick={this.handlePropertyClick}
        />
      </div>
    )
  }

  renderVocabularyProperty() {
    const { terms } = this.state

    let options = []
    if (terms) {
      options = terms.map(term => ({
        value: term.code,
        label: term.label
      }))
      options.unshift({
        value: '',
        label: ''
      })
    }

    return (
      <SelectField
        label={this.getLabel()}
        description={this.getDescription()}
        mandatory={this.getMandatory()}
        options={options}
        metadata={this.getMetadata()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
      />
    )
  }

  renderMaterialProperty() {
    const { materials } = this.state

    let options = []
    if (materials) {
      options = materials.map(material => ({
        value: material.code
      }))
      options.unshift({
        value: '',
        label: ''
      })
    }

    return (
      <SelectField
        label={this.getLabel()}
        description={this.getDescription()}
        mandatory={this.getMandatory()}
        options={options}
        metadata={this.getMetadata()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
      />
    )
  }

  getCode() {
    return this.props.property.code || EMPTY
  }

  getLabel() {
    return this.props.property.label || EMPTY
  }

  getDescription() {
    return this.props.property.description || EMPTY
  }

  getDataType() {
    return this.props.property.dataType
  }

  getMandatory() {
    return this.props.property.mandatory
  }

  getMetadata() {
    const styles = this.getStyles()

    return (
      <React.Fragment>
        [
        <span
          data-part='code'
          className={styles.code}
          onClick={this.handlePropertyClick}
        >
          {this.getCode()}
        </span>
        ][
        <span
          data-part='dataType'
          className={styles.dataType}
          onClick={this.handlePropertyClick}
        >
          {this.getDataType()}
        </span>
        ]
      </React.Fragment>
    )
  }

  getStyles() {
    const { property, selection, classes } = this.props

    let styles = {}

    const parts = ['code', 'label', 'dataType', 'mandatory', 'description']
    const selectedPart =
      selection &&
      selection.type === 'property' &&
      selection.params.id === property.id &&
      selection.params.part

    parts.forEach(part => {
      const partStyles = []

      if (part === selectedPart) {
        partStyles.push(classes.partSelected)
      } else {
        partStyles.push(classes.partNotSelected)
      }

      const partValue = property[part]

      if (!partValue) {
        partStyles.push(classes.partEmpty)
      }

      styles = {
        ...styles,
        [part]: partStyles.join(' ')
      }
    })

    if (!property.showInEditView) {
      styles = {
        ...styles,
        container: classes.hidden
      }
    }

    return styles
  }
}

export default _.flow(withStyles(styles))(ObjectTypePreviewProperty)
