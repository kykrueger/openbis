import React from 'react'

export default class VocabularyForm extends React.PureComponent {
  render() {
    const { object } = this.props
    return <div>Vocabulary Form {object.id}</div>
  }
}
