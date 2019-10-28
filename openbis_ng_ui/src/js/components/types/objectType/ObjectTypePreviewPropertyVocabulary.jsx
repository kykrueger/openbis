import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { facade, dto } from '../../../services/openbis.js'
import SelectField from '../../common/form/SelectField.jsx'
import ObjectTypePreviewPropertyMetadata from './ObjectTypePreviewPropertyMetadata.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewPropertyVocabulary extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
  }

  componentDidMount() {
    this.load()
  }

  componentDidUpdate(prevProps) {
    if (this.props.property.vocabulary !== prevProps.property.vocabulary) {
      this.load()
    }
  }

  load() {
    if (this.props.property.vocabulary) {
      let criteria = new dto.VocabularyTermSearchCriteria()
      let fo = new dto.VocabularyTermFetchOptions()

      criteria
        .withVocabulary()
        .withCode()
        .thatEquals(this.props.property.vocabulary)

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

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyVocabulary.render')

    const { property } = this.props
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
        label={property.label}
        description={property.description}
        mandatory={property.mandatory}
        transparent={!property.visible}
        metadata={<ObjectTypePreviewPropertyMetadata property={property} />}
        options={options}
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyVocabulary)
