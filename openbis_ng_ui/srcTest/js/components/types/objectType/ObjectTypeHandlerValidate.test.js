import ObjectTypeHandlerValidate from '@src/js/components/types/objectType/ObjectTypeHandlerValidate.js'
import ComponentState from '@srcTest/js/common/ComponentState.js'
import openbis from '@srcTest/js/services/openbis.js'

beforeEach(() => {
  jest.resetAllMocks()
})

describe('ObjectTypeHandlerValidateTest', () => {
  test('validation enabled with autofocus fails', done => {
    const componentState = new ComponentState({
      validate: false,
      selection: {
        type: 'testtype'
      },
      type: { usages: 1 },
      properties: [
        { id: 'property-0' },
        { id: 'property-1', dataType: openbis.DataType.CONTROLLEDVOCABULARY },
        { id: 'property-2', dataType: openbis.DataType.MATERIAL },
        { id: 'property-3', mandatory: true }
      ]
    })

    execute(componentState, true, true).then(result => {
      componentState.assertState({
        validate: true,
        selection: {
          type: 'type',
          params: {
            part: 'code'
          }
        },
        type: {
          usages: 1,
          errors: {
            code: 'Code cannot be empty',
            generatedCodePrefix: 'Generated code prefix cannot be empty'
          }
        },
        properties: [
          {
            id: 'property-0',
            errors: {
              code: 'Code cannot be empty',
              dataType: 'Data Type cannot be empty',
              description: 'Description cannot be empty',
              label: 'Label cannot be empty'
            }
          },
          {
            id: 'property-1',
            dataType: openbis.DataType.CONTROLLEDVOCABULARY,
            errors: {
              code: 'Code cannot be empty',
              description: 'Description cannot be empty',
              label: 'Label cannot be empty',
              vocabulary: 'Vocabulary cannot be empty'
            }
          },
          {
            id: 'property-2',
            dataType: openbis.DataType.MATERIAL,
            errors: {
              code: 'Code cannot be empty',
              description: 'Description cannot be empty',
              label: 'Label cannot be empty',
              materialType: 'Material Type cannot be empty'
            }
          },
          {
            id: 'property-3',
            mandatory: true,
            errors: {
              code: 'Code cannot be empty',
              dataType: 'Data Type cannot be empty',
              description: 'Description cannot be empty',
              label: 'Label cannot be empty',
              initialValueForExistingEntities: 'Initial Value cannot be empty'
            }
          }
        ]
      })
      expect(result).toBe(false)
      done()
    })
  })

  test('validation enabled without autofocus fails', done => {
    const componentState = new ComponentState({
      validate: false,
      selection: {
        type: 'testtype'
      },
      type: {},
      properties: [{ id: 'property-0' }]
    })

    execute(componentState, true, false).then(result => {
      componentState.assertState({
        validate: true,
        selection: {
          type: 'testtype'
        },
        type: {
          errors: {
            code: 'Code cannot be empty',
            generatedCodePrefix: 'Generated code prefix cannot be empty'
          }
        },
        properties: [
          {
            id: 'property-0',
            errors: {
              code: 'Code cannot be empty',
              dataType: 'Data Type cannot be empty',
              description: 'Description cannot be empty',
              label: 'Label cannot be empty'
            }
          }
        ]
      })
      expect(result).toBe(false)
      done()
    })
  })

  test('validation enabled with autofocus succeeds', done => {
    const componentState = new ComponentState({
      validate: false,
      selection: {
        type: 'testtype'
      },
      type: { code: 'TYPE_CODE', generatedCodePrefix: 'TYPE_CODE_PREFIX' },
      properties: [
        {
          id: 'property-0',
          code: 'PROPERTY_CODE',
          dataType: 'PROPERTY_DATA_TYPE',
          description: 'PROPERTY_DESCRIPTION',
          label: 'PROPERTY_LABEL'
        }
      ]
    })

    execute(componentState, true, true).then(result => {
      componentState.assertState({
        validate: true,
        selection: {
          type: 'testtype'
        },
        type: {
          code: 'TYPE_CODE',
          generatedCodePrefix: 'TYPE_CODE_PREFIX',
          errors: {}
        },
        properties: [
          {
            id: 'property-0',
            code: 'PROPERTY_CODE',
            dataType: 'PROPERTY_DATA_TYPE',
            description: 'PROPERTY_DESCRIPTION',
            label: 'PROPERTY_LABEL',
            errors: {}
          }
        ]
      })
      expect(result).toBe(true)
      done()
    })
  })

  test('validation enabled without autofocus succeeds', done => {
    const componentState = new ComponentState({
      validate: false,
      selection: {
        type: 'testtype'
      },
      type: { code: 'TYPE_CODE', generatedCodePrefix: 'TYPE_CODE_PREFIX' },
      properties: [
        {
          id: 'property-0',
          code: 'PROPERTY_CODE',
          dataType: 'PROPERTY_DATA_TYPE',
          description: 'PROPERTY_DESCRIPTION',
          label: 'PROPERTY_LABEL'
        }
      ]
    })

    execute(componentState, true, false).then(result => {
      componentState.assertState({
        validate: true,
        selection: {
          type: 'testtype'
        },
        type: {
          code: 'TYPE_CODE',
          generatedCodePrefix: 'TYPE_CODE_PREFIX',
          errors: {}
        },
        properties: [
          {
            id: 'property-0',
            code: 'PROPERTY_CODE',
            dataType: 'PROPERTY_DATA_TYPE',
            description: 'PROPERTY_DESCRIPTION',
            label: 'PROPERTY_LABEL',
            errors: {}
          }
        ]
      })
      expect(result).toBe(true)
      done()
    })
  })

  test('validation disabled', done => {
    const componentState = new ComponentState({
      validate: false,
      selection: {
        type: 'testtype'
      },
      type: {},
      properties: [{ id: 'property-0' }]
    })

    execute(componentState, false, true).then(result => {
      componentState.assertState({
        validate: false,
        selection: {
          type: 'testtype'
        },
        type: {},
        properties: [{ id: 'property-0' }]
      })
      expect(result).toBe(true)
      done()
    })
  })
})

const execute = (componentState, enabled, autofocus) => {
  const handler = new ObjectTypeHandlerValidate(
    componentState.getGetState(),
    componentState.getSetState()
  )
  return handler.setEnabled(enabled).then(() => {
    return handler.execute(autofocus)
  })
}
