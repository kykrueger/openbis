import React from 'react'
import _ from 'lodash'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'
import * as selectors from '../../../store/selectors/selectors.js'
import * as actions from '../../../store/actions/actions.js'

import ContentTabs from './ContentTabs.jsx'

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    width: '100px'
  },
  component: {
    flex: '1 1 100%',
    overflow: 'auto'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none',
  }
}

function mapStateToProps(state, ownProps){
  return {
    openObjects: selectors.getOpenObjects(state, ownProps.page),
    changedObjects: selectors.getChangedObjects(state, ownProps.page),
    selectedObject: selectors.getSelectedObject(state, ownProps.page)
  }
}

function mapDispatchToProps(dispatch, ownProps){
  return {
    objectSelect: (type, id) => { dispatch(actions.objectOpen(ownProps.page, type, id)) },
    objectClose: (type, id) => { dispatch(actions.objectClose(ownProps.page, type, id)) }
  }
}

class Content extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Content.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <ContentTabs
          objects={this.props.openObjects}
          changedObjects={this.props.changedObjects}
          selectedObject={this.props.selectedObject}
          objectSelect={this.props.objectSelect}
          objectClose={this.props.objectClose} />
        {
          this.props.openObjects.map(object => {
            let ObjectComponent = this.props.objectTypeToComponent[object.type]
            if(ObjectComponent){
              let key = object.type + '/' + object.id
              let visible = _.isEqual(object, this.props.selectedObject)
              return (
                <div key={key} className={util.classNames(classes.component, visible ? classes.visible : classes.hidden)}>
                  <ObjectComponent objectId={object.id} />
                </div>
              )
            }
          })
        }
      </div>
    )
  }
}

export default _.flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(Content)
