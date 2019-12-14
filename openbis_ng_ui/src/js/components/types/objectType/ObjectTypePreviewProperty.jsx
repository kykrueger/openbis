import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import SelectField from '../../common/form/SelectField.jsx'
import { dto } from '../../../services/openbis.js'
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
    opacity: 0.7
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
    this.state = {
      values: {}
    }
    this.handleDraggableClick = this.handleDraggableClick.bind(this)
    this.handlePropertyClick = this.handlePropertyClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  componentDidMount() {
    const { dataType } = this.props.property

    if (dataType === dto.DataType.MATERIAL) {
      this.loadMaterials()
    } else if (dataType === dto.DataType.CONTROLLEDVOCABULARY) {
      this.loadVocabularyTerms()
    }
  }

  componentDidUpdate(prevProps) {
    let { property: prevProperty } = prevProps
    let { property } = this.props

    if (property.materialType !== prevProperty.materialType) {
      this.loadMaterials()
    } else if (property.vocabulary !== prevProperty.vocabulary) {
      this.loadVocabularyTerms()
    }
  }

  loadMaterials() {
    const { facade, property } = this.props

    if (property.materialType) {
      return facade
        .loadMaterials(property.materialType)
        .then(result => {
          this.setState(() => ({
            materials: result.objects
          }))
        })
        .catch(error => {
          facade.catch(error)
        })
    } else {
      this.setState(() => ({
        materials: null
      }))
    }
  }

  loadVocabularyTerms() {
    const { facade, property } = this.props

    if (property.vocabulary) {
      return facade
        .loadVocabularyTerms(property.vocabulary)
        .then(result => {
          this.setState(() => ({
            terms: result.objects
          }))
        })
        .catch(error => {
          facade.catch(error)
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

  handleChange(event) {
    const name = event.target.name
    const value = event.target.value

    this.setState(state => ({
      values: {
        ...state.values,
        [name]: value
      }
    }))
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

    if (
      dataType === dto.DataType.VARCHAR ||
      dataType === dto.DataType.MULTILINE_VARCHAR ||
      dataType === dto.DataType.HYPERLINK ||
      dataType === dto.DataType.TIMESTAMP ||
      dataType === dto.DataType.XML
    ) {
      return this.renderVarcharProperty()
    } else if (
      dataType === dto.DataType.REAL ||
      dataType === dto.DataType.INTEGER
    ) {
      return this.renderNumberProperty()
    } else if (dataType === dto.DataType.BOOLEAN) {
      return this.renderBooleanProperty()
    } else if (dataType === dto.DataType.CONTROLLEDVOCABULARY) {
      return this.renderVocabularyProperty()
    } else if (dataType === dto.DataType.MATERIAL) {
      return this.renderMaterialProperty()
    } else {
      return <span>Data type not supported yet</span>
    }
  }

  renderVarcharProperty() {
    const { property } = this.props
    const { values } = this.state
    return (
      <TextField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={values[property.id]}
        mandatory={this.getMandatory()}
        multiline={this.getMultiline()}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderNumberProperty() {
    const { property } = this.props
    const { values } = this.state
    return (
      <TextField
        type='number'
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={values[property.id]}
        mandatory={this.getMandatory()}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderBooleanProperty() {
    const { property } = this.props
    const { values } = this.state
    return (
      <div>
        <CheckboxField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={values[property.id]}
          mandatory={this.getMandatory()}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderVocabularyProperty() {
    const { property } = this.props
    const { terms, values } = this.state

    let options = []

    if (terms) {
      options = terms.map(term => ({
        value: term.code,
        label: term.label
      }))
      options.unshift({})
    }

    return (
      <SelectField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={values[property.id]}
        mandatory={this.getMandatory()}
        options={options}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderMaterialProperty() {
    const { property } = this.props
    const { materials, values } = this.state

    let options = []

    if (materials) {
      options = materials.map(material => ({
        value: material.code
      }))
      options.unshift({})
    }

    return (
      <SelectField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={values[property.id]}
        mandatory={this.getMandatory()}
        options={options}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
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

  getMultiline() {
    return (
      this.props.property.dataType === dto.DataType.MULTILINE_VARCHAR ||
      this.props.property.dataType === dto.DataType.XML
    )
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

  getError() {
    const errors = this.props.property.errors
    if (_.isEmpty(errors)) {
      return null
    } else {
      return 'Property configuration is incorrect'
    }
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
