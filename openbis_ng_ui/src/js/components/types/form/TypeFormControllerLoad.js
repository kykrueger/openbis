import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import util from '@src/js/common/util.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(object),
      this._loadType(object, isNew)
    ])
  }

  async _loadDictionaries(object) {
    const [
      validationPlugins,
      dynamicPlugins,
      vocabularies,
      materialTypes,
      globalPropertyTypes
    ] = await Promise.all([
      this.facade.loadValidationPlugins(object.type),
      this.facade.loadDynamicPlugins(object.type),
      this.facade.loadVocabularies(),
      this.facade.loadMaterialTypes(),
      this.facade.loadGlobalPropertyTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        validationPlugins,
        dynamicPlugins,
        vocabularies,
        materialTypes,
        globalPropertyTypes
      }
    }))
  }

  async _loadType(object, isNew) {
    let loadedType = null
    let loadedUsages = null
    let loadedAssignments = null

    if (!isNew) {
      ;[loadedType, loadedUsages, loadedAssignments] = await Promise.all([
        this.facade.loadType(object),
        this.facade.loadUsages(object),
        this.facade.loadAssignments(object)
      ])
      if (!loadedType) {
        return
      }
    }

    const sections = []
    const properties = []
    let section = null
    let property = null
    let sectionsCounter = 0
    let propertiesCounter = 0

    if (loadedType && loadedType.propertyAssignments) {
      loadedType.propertyAssignments.forEach(loadedAssignment => {
        property = this._createProperty(
          'property-' + propertiesCounter++,
          loadedType,
          loadedAssignment,
          loadedUsages,
          loadedAssignments
        )
        properties.push(property)

        if (
          !section ||
          section.name.value !== util.trim(loadedAssignment.section)
        ) {
          section = this._createSection(
            'section-' + sectionsCounter++,
            loadedAssignment
          )
          sections.push(section)
        }

        section.properties.push(property.id)
        property.section = section.id
        property.original = {
          ...property
        }
      })
    }

    const type = this._createType(loadedType, loadedUsages)

    if (loadedType) {
      type.original = {
        ...type,
        properties
      }
    }

    const selection = this._createSelection(sections)

    return this.context.setState(() => ({
      type,
      properties,
      propertiesCounter,
      sections,
      sectionsCounter,
      selection: selection,
      usages: loadedUsages,
      assignments: loadedAssignments,
      removeSectionDialogOpen: false,
      removePropertyDialogOpen: false,
      unsavedChangesDialogOpen: false
    }))
  }

  _createType(loadedType, loadedUsages) {
    const strategy = this._getStrategy()
    const type = {
      code: this.createField({
        value: _.get(loadedType, 'code', null),
        enabled: loadedType === null
      }),
      objectType: this.createField({
        value: this.object.type
      }),
      description: this.createField({
        value: _.get(loadedType, 'description', null)
      }),
      validationPlugin: this.createField({
        value: _.get(loadedType, 'validationPlugin.name', null)
      }),
      usages: (loadedUsages && loadedUsages.type) || 0,
      errors: 0
    }
    strategy.setTypeAttributes(type, loadedType, loadedUsages)
    return type
  }

  _createSection(id, loadedAssignment) {
    return {
      id: id,
      name: this.createField({
        value: util.trim(loadedAssignment.section)
      }),
      properties: []
    }
  }

  _createProperty(
    id,
    loadedType,
    loadedAssignment,
    loadedUsages,
    loadedAssignments
  ) {
    const propertyType = loadedAssignment.propertyType

    const code = _.get(propertyType, 'code', null)
    const dataType = _.get(propertyType, 'dataType', null)
    const scope = code.startsWith(loadedType.code + '.') ? 'local' : 'global'

    const assignments =
      (loadedAssignments && loadedAssignments[propertyType.code]) || 0

    const usagesLocal =
      (loadedUsages &&
        loadedUsages.propertyLocal &&
        loadedUsages.propertyLocal[propertyType.code]) ||
      0
    const usagesGlobal =
      (loadedUsages &&
        loadedUsages.propertyGlobal &&
        loadedUsages.propertyGlobal[propertyType.code]) ||
      0

    const enabled = usagesGlobal === 0 && assignments <= 1

    return {
      id: id,
      scope: this.createField({
        value: scope,
        enabled: false
      }),
      code: this.createField({
        value: code,
        enabled: false
      }),
      label: this.createField({
        value: _.get(propertyType, 'label', null)
      }),
      description: this.createField({
        value: _.get(propertyType, 'description', null)
      }),
      dataType: this.createField({
        value: dataType,
        enabled
      }),
      plugin: this.createField({
        value: _.get(loadedAssignment, 'plugin.name', null),
        enabled
      }),
      vocabulary: this.createField({
        value: _.get(propertyType, 'vocabulary.code', null),
        visible: dataType === openbis.DataType.CONTROLLEDVOCABULARY,
        enabled
      }),
      materialType: this.createField({
        value: _.get(propertyType, 'materialType.code', null),
        visible: dataType === openbis.DataType.MATERIAL,
        enabled
      }),
      schema: this.createField({
        value: _.get(propertyType, 'schema', null),
        visible: dataType === openbis.DataType.XML
      }),
      transformation: this.createField({
        value: _.get(propertyType, 'transformation', null),
        visible: dataType === openbis.DataType.XML
      }),
      mandatory: this.createField({
        value: _.get(loadedAssignment, 'mandatory', false)
      }),
      showInEditView: this.createField({
        value: _.get(loadedAssignment, 'showInEditView', false)
      }),
      showRawValueInForms: this.createField({
        value: _.get(loadedAssignment, 'showRawValueInForms', false)
      }),
      initialValueForExistingEntities: this.createField({
        visible: false
      }),
      assignments,
      usagesLocal,
      usagesGlobal,
      errors: 0
    }
  }

  _createSelection(newSections) {
    const {
      selection: oldSelection,
      sections: oldSections
    } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === 'section') {
      let sectionIndex = -1

      oldSections.forEach((oldSection, i) => {
        if (oldSection.id === oldSelection.params.id) {
          sectionIndex = i
        }
      })

      if (sectionIndex >= 0 && sectionIndex < newSections.length) {
        const newSection = newSections[sectionIndex]
        return {
          type: 'section',
          params: {
            id: newSection.id,
            part: oldSelection.params.part
          }
        }
      }
    } else if (oldSelection.type === 'property') {
      let sectionIndex = -1
      let propertyIndex = -1

      oldSections.forEach((oldSection, i) => {
        oldSection.properties.forEach((oldProperty, j) => {
          if (oldProperty === oldSelection.params.id) {
            sectionIndex = i
            propertyIndex = j
          }
        })
      })

      if (sectionIndex >= 0 && sectionIndex < newSections.length) {
        const newSection = newSections[sectionIndex]
        if (
          propertyIndex >= 0 &&
          propertyIndex < newSection.properties.length
        ) {
          const newProperty = newSection.properties[propertyIndex]
          return {
            type: 'property',
            params: {
              id: newProperty,
              part: oldSelection.params.part
            }
          }
        }
      }
    } else {
      return null
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy(this))
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy(this))
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  constructor(executor) {
    this.executor = executor
  }

  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      listable: this.executor.createField({
        value: _.get(loadedType, 'listable', false)
      }),
      showContainer: this.executor.createField({
        value: _.get(loadedType, 'showContainer', false)
      }),
      showParents: this.executor.createField({
        value: _.get(loadedType, 'showParents', false)
      }),
      showParentMetadata: this.executor.createField({
        value: _.get(loadedType, 'showParentMetadata', false)
      }),
      autoGeneratedCode: this.executor.createField({
        value: _.get(loadedType, 'autoGeneratedCode', false)
      }),
      generatedCodePrefix: this.executor.createField({
        value: _.get(loadedType, 'generatedCodePrefix', null)
      }),
      subcodeUnique: this.executor.createField({
        value: _.get(loadedType, 'subcodeUnique', false)
      })
    })
  }
}

class CollectionTypeStrategy {
  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  constructor(executor) {
    this.executor = executor
  }

  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      mainDataSetPattern: this.executor.createField({
        value: _.get(loadedType, 'mainDataSetPattern', null)
      }),
      mainDataSetPath: this.executor.createField({
        value: _.get(loadedType, 'mainDataSetPath', null)
      }),
      disallowDeletion: this.executor.createField({
        value: _.get(loadedType, 'disallowDeletion', false)
      })
    })
  }
}

class MaterialTypeStrategy {
  setTypeAttributes() {}
}
