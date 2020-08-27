import _ from 'lodash'
import React from 'react'
import { Draggable } from 'react-beautiful-dnd'
import { withStyles } from '@material-ui/core/styles'
import Message from '@src/js/components/common/form/Message.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import logger from '@src/js/common/logger.js'
import util from '@src/js/common/util.js'

const EMPTY = 'empty'

const styles = theme => ({
  draggable: {
    width: '100%',
    cursor: 'pointer',
    padding: theme.spacing(1),
    boxSizing: 'border-box',
    borderWidth: '2px',
    borderStyle: 'solid',
    borderColor: theme.palette.background.paper,
    backgroundColor: theme.palette.background.paper,
    '&:last-child': {
      marginBottom: 0
    },
    '&:hover': {
      borderColor: theme.palette.border.primary
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
      borderColor: theme.palette.border.primary
    }
  }
})

class TypeFormPreviewProperty extends React.PureComponent {
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

    if (dataType.value === openbis.DataType.MATERIAL) {
      this.loadMaterials()
    } else if (dataType.value === openbis.DataType.SAMPLE) {
      this.loadSamples()
    } else if (dataType.value === openbis.DataType.CONTROLLEDVOCABULARY) {
      this.loadVocabularyTerms()
    }
  }

  componentDidUpdate(prevProps) {
    let { property: prevProperty } = prevProps
    let { property } = this.props

    if (property.materialType.value !== prevProperty.materialType.value) {
      this.loadMaterials()
    } else if (property.sampleType.value !== prevProperty.sampleType.value) {
      this.loadSamples()
    } else if (property.vocabulary.value !== prevProperty.vocabulary.value) {
      this.loadVocabularyTerms()
    }
  }

  loadMaterials() {
    const { controller, property } = this.props

    if (property.materialType.value) {
      return controller
        .getFacade()
        .loadMaterials(property.materialType.value)
        .then(materials => {
          this.setState(() => ({
            materials
          }))
        })
        .catch(error => {
          controller.getContext().dispatch(actions.errorChange(error))
        })
    } else {
      this.setState(() => ({
        materials: null
      }))
    }
  }

  loadSamples() {
    const { controller, property } = this.props

    return controller
      .getFacade()
      .loadSamples(property.sampleType.value)
      .then(samples => {
        this.setState(() => ({
          samples
        }))
      })
      .catch(error => {
        controller.getContext().dispatch(actions.errorChange(error))
      })
  }

  loadVocabularyTerms() {
    const { controller, property } = this.props

    if (property.vocabulary.value) {
      return controller
        .getFacade()
        .loadVocabularyTerms(property.vocabulary.value)
        .then(terms => {
          this.setState(() => ({
            terms
          }))
        })
        .catch(error => {
          controller.getContext().dispatch(actions.errorChange(error))
        })
    } else {
      this.setState(() => ({
        terms: null
      }))
    }
  }

  handleDraggableClick(event) {
    let newSelection = {
      type: 'property',
      params: {
        id: this.props.property.id
      }
    }
    this.handleClick(event, newSelection)
  }

  handlePropertyClick(event) {
    let newSelection = {
      type: 'property',
      params: {
        id: this.props.property.id
      }
    }
    if (event.target.dataset && event.target.dataset.part) {
      newSelection.params.part = event.target.dataset.part
    }
    this.handleClick(event, newSelection)
  }

  handleClick(event, newSelection) {
    const { selection } = this.props

    event.stopPropagation()

    if (_.isEqual(selection, newSelection)) {
      this.props.onSelectionChange()
    } else {
      this.props.onSelectionChange(newSelection.type, newSelection.params)
    }
  }

  handleChange(event) {
    this.props.onChange('preview', {
      field: this.props.property.id,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormPreviewProperty.render')

    let { mode, property, selection, index, classes } = this.props

    const selected =
      selection &&
      selection.type === 'property' &&
      selection.params.id === property.id

    return (
      <Draggable
        draggableId={property.id}
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
    const dataType = this.props.property.dataType.value

    if (
      dataType === openbis.DataType.VARCHAR ||
      dataType === openbis.DataType.MULTILINE_VARCHAR ||
      dataType === openbis.DataType.HYPERLINK ||
      dataType === openbis.DataType.TIMESTAMP ||
      dataType === openbis.DataType.XML
    ) {
      return this.renderVarcharProperty()
    } else if (
      dataType === openbis.DataType.REAL ||
      dataType === openbis.DataType.INTEGER
    ) {
      return this.renderNumberProperty()
    } else if (dataType === openbis.DataType.BOOLEAN) {
      return this.renderBooleanProperty()
    } else if (dataType === openbis.DataType.CONTROLLEDVOCABULARY) {
      return this.renderVocabularyProperty()
    } else if (dataType === openbis.DataType.MATERIAL) {
      return this.renderMaterialProperty()
    } else if (dataType === openbis.DataType.SAMPLE) {
      return this.renderSampleProperty()
    } else {
      if (dataType) {
        return this.renderPropertyNotSupported()
      } else {
        return this.renderPropertyWithoutDataType()
      }
    }
  }

  renderPropertyNotSupported() {
    return (
      <Message type='warning'>
        The selected data type is not supported yet.
      </Message>
    )
  }

  renderPropertyWithoutDataType() {
    return (
      <Message type='info'>
        Please select a data type to display the field preview.
      </Message>
    )
  }

  renderVarcharProperty() {
    const { property, value, mode } = this.props
    return (
      <TextField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={value}
        mandatory={this.getMandatory()}
        multiline={this.getMultiline()}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        mode='edit'
        disabled={mode !== 'edit'}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderNumberProperty() {
    const { property, value, mode } = this.props
    return (
      <TextField
        type='number'
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={value}
        mandatory={this.getMandatory()}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        mode='edit'
        disabled={mode !== 'edit'}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderBooleanProperty() {
    const { property, value, mode } = this.props
    return (
      <div>
        <CheckboxField
          name={property.id}
          label={this.getLabel()}
          description={this.getDescription()}
          value={value}
          mandatory={this.getMandatory()}
          metadata={this.getMetadata()}
          error={this.getError()}
          styles={this.getStyles()}
          mode='edit'
          disabled={mode !== 'edit'}
          onClick={this.handlePropertyClick}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderVocabularyProperty() {
    const { property, value, mode } = this.props
    const { terms } = this.state

    let options = []

    if (terms) {
      options = terms.map(term => ({
        value: term.code,
        label: term.label
      }))
    }

    return (
      <SelectField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={value}
        mandatory={this.getMandatory()}
        options={options}
        emptyOption={{
          label: '(vocabulary terms preview)',
          selectable: false
        }}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        mode='edit'
        disabled={mode !== 'edit'}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderMaterialProperty() {
    const { property, value, mode } = this.props
    const { materials } = this.state

    let options = []

    if (materials) {
      options = materials.map(material => ({
        value: material.code
      }))
    }

    return (
      <SelectField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={value}
        mandatory={this.getMandatory()}
        options={options}
        emptyOption={{
          label: '(materials preview)',
          selectable: false
        }}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        mode='edit'
        disabled={mode !== 'edit'}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  renderSampleProperty() {
    const { property, value, mode } = this.props
    const { samples } = this.state

    let options = []

    if (samples) {
      options = samples.map(sample => ({
        value: sample.identifier.identifier
      }))
    }

    return (
      <SelectField
        name={property.id}
        label={this.getLabel()}
        description={this.getDescription()}
        value={value}
        mandatory={this.getMandatory()}
        options={options}
        emptyOption={{
          label: '(samples preview)',
          selectable: false
        }}
        metadata={this.getMetadata()}
        error={this.getError()}
        styles={this.getStyles()}
        mode='edit'
        disabled={mode !== 'edit'}
        onClick={this.handlePropertyClick}
        onChange={this.handleChange}
      />
    )
  }

  getCode() {
    return this.props.property.code.value || EMPTY
  }

  getLabel() {
    return this.props.property.label.value || EMPTY
  }

  getDescription() {
    return this.props.property.description.value || EMPTY
  }

  getDataType() {
    return this.props.property.dataType.value || EMPTY
  }

  getMandatory() {
    return this.props.property.mandatory.value
  }

  getMultiline() {
    return (
      this.props.property.dataType.value ===
        openbis.DataType.MULTILINE_VARCHAR ||
      this.props.property.dataType.value === openbis.DataType.XML
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
    if (this.props.property.errors > 0) {
      return 'Property configuration is incorrect'
    } else {
      return null
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

      const partValue = property[part].value

      if (!partValue) {
        partStyles.push(classes.partEmpty)
      }

      styles = {
        ...styles,
        [part]: partStyles.join(' ')
      }
    })

    if (!property.showInEditView.value) {
      styles = {
        ...styles,
        container: classes.hidden
      }
    }

    return styles
  }
}

export default _.flow(withStyles(styles))(TypeFormPreviewProperty)
