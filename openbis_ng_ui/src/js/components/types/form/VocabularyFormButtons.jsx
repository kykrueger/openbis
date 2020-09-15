import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
import users from '@src/js/common/consts/users.js'
import logger from '@src/js/common/logger.js'

class VocabularyFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, vocabulary } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={vocabulary.id ? onCancel : null}
        renderAdditionalButtons={classes =>
          this.renderAdditionalButtons(classes)
        }
      />
    )
  }

  renderAdditionalButtons(classes) {
    const { onAdd, onRemove } = this.props

    return (
      <React.Fragment>
        <Button
          name='addTerm'
          label='Add Term'
          styles={{ root: classes.button }}
          onClick={onAdd}
        />
        <Button
          name='removeTerm'
          label='Remove Term'
          styles={{ root: classes.button }}
          disabled={!this.isNonSystemInternalTermSelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isNonSystemInternalTermSelected() {
    const { selection, vocabulary, terms } = this.props

    if (selection && selection.type === VocabularyFormSelectionType.TERM) {
      const term = terms.find(term => term.id === selection.params.id)
      return !(
        vocabulary.internal.value && term.registrator.value === users.SYSTEM
      )
    } else {
      return false
    }
  }
}

export default VocabularyFormButtons
