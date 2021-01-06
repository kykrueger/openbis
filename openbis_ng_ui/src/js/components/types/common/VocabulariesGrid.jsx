import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import VocabularyLink from '@src/js/components/common/link/VocabularyLink.jsx'
import logger from '@src/js/common/logger.js'

class VocabulariesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'VocabulariesGrid.render')

    const {
      id,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header={'Vocabulary Types'}
        columns={[
          {
            name: 'code',
            label: 'Code',
            sort: 'asc',
            getValue: ({ row }) => row.code,
            renderValue: ({ row }) => {
              return <VocabularyLink vocabularyCode={row.code} />
            }
          },
          {
            name: 'description',
            label: 'Description',
            getValue: ({ row }) => row.description
          },
          {
            name: 'urlTemplate',
            label: 'URL template',
            getValue: ({ row }) => row.urlTemplate
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default VocabulariesGrid
