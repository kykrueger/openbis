import React from 'react'
import TextField from '@material-ui/core/TextField'
import { withStyles } from '@material-ui/core/styles'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = () => ({
  visible: {
    opacity: 1
  },
  hidden: {
    opacity: 0.5
  }
})

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

    const { property, classes } = this.props
    const { terms } = this.state

    return (
      <TextField
        select
        error={property.mandatory}
        SelectProps={{
          native: true
        }}
        InputLabelProps={{
          shrink: true
        }}
        label={property.label}
        helperText={property.description}
        fullWidth={true}
        className={property.visible ? classes.visible : classes.hidden}
        variant='filled'
      >
        {terms &&
          terms.map(term => (
            <option key={term.code} value={term.code}>
              {term.label || term.code}
            </option>
          ))}
        <React.Fragment></React.Fragment>
      </TextField>
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyVocabulary)
