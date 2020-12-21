import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

const LOADED_OPTIONS_COUNT = 100

class EntityAutocompleterField extends React.PureComponent {
  constructor(props) {
    super(props)

    autoBind(this)

    this.state = {
      loading: false,
      inputValue: this.getOptionLabel(props.value),
      options: []
    }
  }

  async load(value) {
    this.cancelScheduledLoad()

    const loadId = setTimeout(async () => {
      try {
        logger.log(
          logger.DEBUG,
          `EntityAutocompleterField - executing load request (id: ${loadId}, value: ${value})`
        )

        const results = await this.loadEntities(value, LOADED_OPTIONS_COUNT)

        let options = []
        if (results.options.length > 0) {
          if (results.totalCount > results.options.length) {
            options.push({
              label: `Showing only the first ${LOADED_OPTIONS_COUNT} results (${results.totalCount} found)`
            })
          }
          results.options.forEach(option => options.push(option))
        } else {
          options = [
            {
              label: 'No entities found'
            }
          ]
        }

        if (loadId === this.state.loadId) {
          logger.log(
            logger.DEBUG,
            `EntityAutocompleterField - received valid load response (id: ${loadId}, value: ${value})`
          )

          this.setState({
            loading: false,
            options
          })
        } else {
          logger.log(
            logger.DEBUG,
            `EntityAutocompleterField - ignoring old load response (id: ${loadId}, value: ${value})`
          )
        }
      } catch (error) {
        this.setState({
          loading: false
        })
      }
    }, 250)

    logger.log(
      logger.DEBUG,
      `EntityAutocompleterField - scheduled load request (id: ${loadId}, value: ${value})`
    )

    this.setState({
      loading: true,
      loadId
    })
  }

  cancelScheduledLoad() {
    const lastLoadId = this.state.loadId

    if (lastLoadId) {
      logger.log(
        logger.DEBUG,
        `EntityAutocompleterField - cancelling scheduled load request (id: ${lastLoadId})`
      )

      clearTimeout(lastLoadId)
      this.setState({
        loadId: null
      })
    }
  }

  async loadEntities(value, count) {
    const { entityKind } = this.props

    if (entityKind === openbis.EntityKind.EXPERIMENT) {
      return await this.loadExperiments(value, count)
    } else if (entityKind === openbis.EntityKind.SAMPLE) {
      return await this.loadSamples(value, count)
    } else if (entityKind === openbis.EntityKind.DATA_SET) {
      return await this.loadDataSets(value, count)
    } else if (entityKind === openbis.EntityKind.MATERIAL) {
      return await this.loadMaterials(value, count)
    } else {
      return []
    }
  }

  async loadExperiments(value, count) {
    const criteria = new openbis.ExperimentSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      //criteria.withCode().thatContains(value)
      criteria.withIdentifier().thatContains(value)
      criteria.withProperty('$NAME').thatContains(value)
    }

    const fo = new openbis.ExperimentFetchOptions()
    fo.from(0).count(count)
    fo.sortBy().identifier().asc()

    const results = await openbis.searchExperiments(criteria, fo)

    return {
      options: results.getObjects().map(object => ({
        label: object.identifier.identifier,
        entityKind: openbis.EntityKind.EXPERIMENT,
        entityId: object.identifier.identifier
      })),
      totalCount: results.totalCount
    }
  }

  async loadSamples(value, count) {
    const criteria = new openbis.SampleSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      //criteria.withCode().thatContains(value)
      criteria.withIdentifier().thatContains(value)
      criteria.withProperty('$NAME').thatContains(value)
    }

    const fo = new openbis.SampleFetchOptions()
    fo.from(0).count(count)
    fo.sortBy().identifier().asc()

    const results = await openbis.searchSamples(criteria, fo)

    return {
      options: results.getObjects().map(object => ({
        label: object.identifier.identifier,
        entityKind: openbis.EntityKind.SAMPLE,
        entityId: object.identifier.identifier
      })),
      totalCount: results.totalCount
    }
  }

  async loadMaterials(value, count) {
    const criteria = new openbis.MaterialSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      criteria.withCode().thatContains(value)
      criteria.withProperty('$NAME').thatContains(value)
    }

    const fo = new openbis.MaterialFetchOptions()
    fo.from(0).count(count)
    fo.sortBy().code().asc()

    const results = await openbis.searchMaterials(criteria, fo)

    return {
      options: results.getObjects().map(object => ({
        label: object.permId.code + ' (' + object.permId.typeCode + ')',
        entityKind: openbis.EntityKind.MATERIAL,
        entityId: {
          code: object.permId.code,
          typeCode: object.permId.typeCode
        }
      })),
      totalCount: results.totalCount
    }
  }

  async loadDataSets(value, count) {
    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      criteria.withCode().thatContains(value)
      criteria.withProperty('$NAME').thatContains(value)

      const experimentCriteria = criteria.withExperiment()
      experimentCriteria.withOrOperator()
      //experimentCriteria.withCode().thatContains(value)
      experimentCriteria.withIdentifier().thatContains(value)
      experimentCriteria.withProperty('$NAME').thatContains(value)

      const sampleCriteria = criteria.withSample()
      sampleCriteria.withOrOperator()
      //sampleCriteria.withCode().thatContains(value)
      sampleCriteria.withIdentifier().thatContains(value)
      sampleCriteria.withProperty('$NAME').thatContains(value)
    }

    const fo = new openbis.DataSetFetchOptions()
    fo.from(0).count(count)
    fo.sortBy().code().asc()

    const results = await openbis.searchDataSets(criteria, fo)

    return {
      options: results.getObjects().map(object => ({
        label: object.code,
        entityKind: openbis.EntityKind.DATA_SET,
        entityId: object.code
      })),
      totalCount: results.totalCount
    }
  }

  handleFocus() {
    this.load(this.state.inputValue)
  }

  handleInputChange(event) {
    this.setState({
      inputValue: event.target.value
    })
    this.load(event.target.value)
  }

  handleBlur(event) {
    const { value, onChange } = this.props
    const { inputValue } = this.state

    const valueTrimmed = value ? this.getOptionLabel(value).trim() : ''
    const inputValueTrimmed = inputValue ? inputValue.trim() : ''

    if (inputValueTrimmed.length === 0 && valueTrimmed.length !== 0) {
      if (onChange) {
        onChange(event)
      }
    } else if (inputValueTrimmed !== valueTrimmed) {
      this.setState({
        inputValue: this.getOptionLabel(value)
      })
    }

    this.cancelScheduledLoad()

    this.setState({
      loading: false,
      options: []
    })
  }

  renderOption(option) {
    return <span>{this.getOptionLabel(option)}</span>
  }

  filterOptions(options) {
    // do not filter options on the client side
    return options
  }

  getOptionLabel(option) {
    if (option) {
      return option.label
    } else {
      return ''
    }
  }

  getOptionSelected(option, value) {
    return (
      _.isEqual(option.entityKind, value.entityKind) &&
      _.isEqual(option.entityId, value.entityId)
    )
  }

  getOptionDisabled(option) {
    return !option.entityId
  }

  render() {
    logger.log(logger.DEBUG, 'EntityAutocompleterField.render')

    const { value } = this.props
    const { loading, inputValue, options } = this.state

    return (
      <AutocompleterField
        {...this.props}
        loading={loading}
        freeSolo={true}
        value={value}
        inputValue={inputValue}
        renderOption={this.renderOption}
        filterOptions={this.filterOptions}
        getOptionLabel={this.getOptionLabel}
        getOptionSelected={this.getOptionSelected}
        getOptionDisabled={this.getOptionDisabled}
        onInputChange={this.handleInputChange}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        options={options}
      />
    )
  }
}

export default withStyles(styles)(EntityAutocompleterField)
