import FilterField from '@src/js/components/common/form/FilterField.jsx'
import BrowserNode from '@src/js/components/common/browser/BrowserNode.jsx'
import BrowserButtons from '@src/js/components/common/browser/BrowserButtons.jsx'

import BaseWrapper from '@srcTest/js/common/wrapper/BaseWrapper.js'
import FilterFieldWrapper from '@srcTest/js/common/wrapper/FilterFieldWrapper.js'

import BrowserNodeWrapper from './BrowserNodeWrapper.js'
import BrowserButtonsWrapper from './BrowserButtonsWrapper.js'

export default class BrowserWrapper extends BaseWrapper {
  getFilter() {
    return new FilterFieldWrapper(this.wrapper.find(FilterField))
  }

  getNodes() {
    return this._getNodes(this.wrapper, [])
  }

  _getNodes(wrapper, nodes) {
    wrapper.children().forEach(childWrapper => {
      if (childWrapper.is(BrowserNode)) {
        nodes.push(new BrowserNodeWrapper(childWrapper))
      }
      this._getNodes(childWrapper, nodes)
    })
    return nodes
  }

  getButtons() {
    return new BrowserButtonsWrapper(this.wrapper.find(BrowserButtons))
  }

  toJSON() {
    return {
      filter: this.getFilter().toJSON(),
      nodes: this.getNodes().map(node => node.toJSON()),
      buttons: this.getButtons().toJSON()
    }
  }
}
