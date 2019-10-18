import React from 'react'
import TextField from '@material-ui/core/TextField'
import { withStyles } from '@material-ui/core/styles'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = () => ({
  visible: {
    opacity: 1
  },
  hidden: {
    opacity: 0.5
  }
})

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

    const { property, classes } = this.props
    const { materials } = this.state

    return (
      <TextField
        select
        error={property.mandatory}
        SelectProps={{
          native: true
        }}
        InputLabelProps={{
          shrink: true
        }}
        label={property.label}
        helperText={property.description}
        fullWidth={true}
        className={property.visible ? classes.visible : classes.hidden}
        variant='filled'
      >
        {materials &&
          materials.map(material => (
            <option key={material.code} value={material.code}>
              {material.code}
            </option>
          ))}
        <React.Fragment></React.Fragment>
      </TextField>
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyMaterial)
