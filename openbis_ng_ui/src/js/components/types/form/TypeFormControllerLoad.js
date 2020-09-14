import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import TypeFormControllerStrategies from '@src/js/components/types/form/TypeFormControllerStrategies.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormPropertyScope from '@src/js/components/types/form/TypeFormPropertyScope.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import users from '@src/js/common/consts/users.js'
import util from '@src/js/common/util.js'

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
      sampleTypes,
      globalPropertyTypes
    ] = await Promise.all([
      this.facade.loadValidationPlugins(object.type),
      this.facade.loadDynamicPlugins(object.type),
      this.facade.loadVocabularies(),
      this.facade.loadMaterialTypes(),
      this.facade.loadSampleTypes(),
      this.facade.loadGlobalPropertyTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        validationPlugins,
        dynamicPlugins,
        vocabularies,
        materialTypes,
        sampleTypes,
        globalPropertyTypes
      }
    }))
  }

  async _loadType(object, isNew) {
    let loadedType = null

    if (!isNew) {
      loadedType = await this.facade.loadType(object)
      if (!loadedType) {
        return
      }
    }

    let [loadedUsages, loadedAssignments] = await Promise.all([
      this.facade.loadUsages(object),
      this.facade.loadAssignments(object)
    ])

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
        property.original = _.cloneDeep(property)
      })
    }

    const type = this._createType(loadedType, loadedUsages)

    if (loadedType) {
      type.original = _.cloneDeep({
        ...type,
        properties
      })
    }

    const selection = this._createSelection(sections)

    return this.context.setState(() => ({
      type,
      properties,
      propertiesCounter,
      sections,
      sectionsCounter,
      preview: {},
      selection: selection,
      usages: loadedUsages,
      assignments: loadedAssignments,
      removeSectionDialogOpen: false,
      removePropertyDialogOpen: false
    }))
  }

  _createType(loadedType, loadedUsages) {
    const strategy = this._getStrategy()
    const type = {
      code: FormUtil.createField({
        value: _.get(loadedType, 'code', null),
        enabled: loadedType === null
      }),
      objectType: FormUtil.createField({
        value: this.object.type
      }),
      description: FormUtil.createField({
        value: _.get(loadedType, 'description', null)
      }),
      validationPlugin: FormUtil.createField({
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
      name: FormUtil.createField({
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
    const internal = _.get(propertyType, 'managedInternally', false)
    const plugin = _.get(loadedAssignment, 'plugin.name', null)

    const scope = code.startsWith(loadedType.code + '.')
      ? TypeFormPropertyScope.LOCAL
      : TypeFormPropertyScope.GLOBAL

    const registratorOfAssignment = _.get(
      loadedAssignment,
      'registrator.userId',
      null
    )
    const registratorOfPropertyType = _.get(
      propertyType,
      'registrator.userId',
      null
    )

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

    const unused = usagesGlobal === 0 && assignments <= 1

    const systemInternalAssignment =
      internal && registratorOfAssignment === users.SYSTEM
    const systemInternalPropertyType =
      internal && registratorOfPropertyType === users.SYSTEM

    return {
      id: id,
      scope: FormUtil.createField({
        value: scope,
        enabled: false
      }),
      code: FormUtil.createField({
        value: code,
        enabled: false
      }),
      internal: FormUtil.createField({
        value: internal,
        visible: false,
        enabled: false
      }),
      label: FormUtil.createField({
        value: _.get(propertyType, 'label', null),
        enabled: !systemInternalPropertyType
      }),
      description: FormUtil.createField({
        value: _.get(propertyType, 'description', null),
        enabled: !systemInternalPropertyType
      }),
      dataType: FormUtil.createField({
        value: dataType,
        enabled: !systemInternalPropertyType
      }),
      schema: FormUtil.createField({
        value: _.get(propertyType, 'schema', null),
        visible: dataType === openbis.DataType.XML,
        enabled: !systemInternalPropertyType
      }),
      transformation: FormUtil.createField({
        value: _.get(propertyType, 'transformation', null),
        visible: dataType === openbis.DataType.XML,
        enabled: !systemInternalPropertyType
      }),
      vocabulary: FormUtil.createField({
        value: _.get(propertyType, 'vocabulary.code', null),
        visible: dataType === openbis.DataType.CONTROLLEDVOCABULARY,
        enabled: unused && !systemInternalPropertyType
      }),
      materialType: FormUtil.createField({
        value: _.get(propertyType, 'materialType.code', null),
        visible: dataType === openbis.DataType.MATERIAL,
        enabled: unused && !systemInternalPropertyType
      }),
      sampleType: FormUtil.createField({
        value: _.get(propertyType, 'sampleType.code', null),
        visible: dataType === openbis.DataType.SAMPLE,
        enabled: unused && !systemInternalPropertyType
      }),
      plugin: FormUtil.createField({
        value: plugin,
        enabled: plugin && !systemInternalAssignment
      }),
      mandatory: FormUtil.createField({
        value: _.get(loadedAssignment, 'mandatory', false),
        enabled: !systemInternalAssignment
      }),
      showInEditView: FormUtil.createField({
        value: _.get(loadedAssignment, 'showInEditView', true),
        enabled: !systemInternalAssignment
      }),
      showRawValueInForms: FormUtil.createField({
        value: _.get(loadedAssignment, 'showRawValueInForms', false),
        enabled: !systemInternalAssignment
      }),
      initialValueForExistingEntities: FormUtil.createField({
        visible: false,
        enabled: !systemInternalAssignment
      }),
      registratorOfAssignment: FormUtil.createField({
        value: registratorOfAssignment,
        visible: false,
        enabled: false
      }),
      registratorOfPropertyType: FormUtil.createField({
        value: registratorOfPropertyType,
        visible: false,
        enabled: false
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
    } else if (oldSelection.type === TypeFormSelectionType.SECTION) {
      let sectionIndex = -1

      oldSections.forEach((oldSection, i) => {
        if (oldSection.id === oldSelection.params.id) {
          sectionIndex = i
        }
      })

      if (sectionIndex >= 0 && sectionIndex < newSections.length) {
        const newSection = newSections[sectionIndex]
        return {
          type: TypeFormSelectionType.SECTION,
          params: {
            id: newSection.id,
            part: oldSelection.params.part
          }
        }
      }
    } else if (oldSelection.type === TypeFormSelectionType.PROPERTY) {
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
            type: TypeFormSelectionType.PROPERTY,
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
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      listable: FormUtil.createField({
        value: _.get(loadedType, 'listable', true)
      }),
      showContainer: FormUtil.createField({
        value: _.get(loadedType, 'showContainer', false)
      }),
      showParents: FormUtil.createField({
        value: _.get(loadedType, 'showParents', true)
      }),
      showParentMetadata: FormUtil.createField({
        value: _.get(loadedType, 'showParentMetadata', false)
      }),
      autoGeneratedCode: FormUtil.createField({
        value: _.get(loadedType, 'autoGeneratedCode', true)
      }),
      generatedCodePrefix: FormUtil.createField({
        value: _.get(loadedType, 'generatedCodePrefix', null)
      }),
      subcodeUnique: FormUtil.createField({
        value: _.get(loadedType, 'subcodeUnique', false)
      })
    })
  }
}

class CollectionTypeStrategy {
  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      mainDataSetPattern: FormUtil.createField({
        value: _.get(loadedType, 'mainDataSetPattern', null)
      }),
      mainDataSetPath: FormUtil.createField({
        value: _.get(loadedType, 'mainDataSetPath', null)
      }),
      disallowDeletion: FormUtil.createField({
        value: _.get(loadedType, 'disallowDeletion', false)
      })
    })
  }
}

class MaterialTypeStrategy {
  setTypeAttributes() {}
}
