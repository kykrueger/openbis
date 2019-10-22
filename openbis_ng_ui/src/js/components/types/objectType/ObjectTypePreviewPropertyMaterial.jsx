import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { facade, dto } from '../../../services/openbis.js'
import SelectField from '../../common/form/SelectField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewPropertyMaterial extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
  }

  componentDidMount() {
    this.load()
  }

  componentDidUpdate(prevProps) {
    if (this.props.property.materialType !== prevProps.property.materialType) {
      this.load()
    }
  }

  load() {
    if (this.props.property.materialType) {
      let criteria = new dto.MaterialSearchCriteria()
      let fo = new dto.MaterialFetchOptions()

      criteria
        .withType()
        .withCode()
        .thatEquals(this.props.property.materialType)

      return facade.searchMaterials(criteria, fo).then(result => {
        this.setState(() => ({
          materials: result.objects
        }))
      })
    } else {
      this.setState(() => ({
        materials: null
      }))
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyMaterial.render')

    const { property } = this.props
    const { materials } = this.state

    let options = []
    if (materials) {
      options = materials.map(material => ({
        value: material.code
      }))
    }

    return (
      <SelectField
        label={property.label}
        description={property.description}
        mandatory={property.mandatory}
        transparent={!property.visible}
        options={options}
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyMaterial)
