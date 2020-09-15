import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import VocabularyFormParametersVocabulary from '@src/js/components/types/form/VocabularyFormParametersVocabulary.jsx'
import VocabularyFormParametersTerm from '@src/js/components/types/form/VocabularyFormParametersTerm.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class VocabularyFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormParameters.render')

    const {
      controller,
      vocabulary,
      terms,
      selection,
      selectedRow,
      mode,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <VocabularyFormParametersVocabulary
          controller={controller}
          vocabulary={vocabulary}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <VocabularyFormParametersTerm
          controller={controller}
          vocabulary={vocabulary}
          terms={terms}
          selection={selection}
          selectedRow={selectedRow}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(VocabularyFormParameters)
