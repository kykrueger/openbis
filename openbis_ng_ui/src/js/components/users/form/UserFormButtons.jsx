import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

class UserFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, user } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={user.id ? onCancel : null}
        renderAdditionalButtons={classes =>
          this.renderAdditionalButtons(classes)
        }
      />
    )
  }

  renderAdditionalButtons(classes) {
    const { onAddGroup, onRemove } = this.props

    return (
      <React.Fragment>
        <Button
          name='addGroup'
          label='Add Group'
          styles={{ root: classes.button }}
          onClick={onAddGroup}
        />
        <Button
          name='remove'
          label='Remove'
          styles={{ root: classes.button }}
          disabled={!this.isGroupSelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isGroupSelected() {
    const { selection } = this.props
    return selection && selection.type === 'group'
  }
}

export default UserFormButtons
