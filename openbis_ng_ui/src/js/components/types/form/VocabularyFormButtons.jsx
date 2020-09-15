import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
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
        onEdit={vocabulary.managedInternally.value ? null : onEdit}
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
          disabled={!this.isTermSelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isTermSelected() {
    const { selection } = this.props
    return selection && selection.type === VocabularyFormSelectionType.TERM
  }
}

export default VocabularyFormButtons
