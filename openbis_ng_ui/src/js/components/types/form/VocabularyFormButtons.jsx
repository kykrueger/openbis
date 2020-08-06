import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import objectType from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class VocabularyFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, object } = this.props

    const existing = object.type === objectType.VOCABULARY_TYPE

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={existing ? onCancel : null}
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
    return selection && selection.type === 'term'
  }
}

export default VocabularyFormButtons
