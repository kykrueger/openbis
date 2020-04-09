export function simulateNodeIconClick(wrapper, id) {
  wrapper
    .findWhere(node => {
      return node.name() === 'BrowserNode' && node.prop('node').id === id
    })
    .find('svg')
    .first()
    .simulate('click')
}

export function simulateFilterChange(wrapper, filter) {
  let input = wrapper.find('FilterField').find('input')
  input.instance().value = filter
  input.simulate('change')
}

export function expectFilter(wrapper, expectedFilter) {
  const actualFilter = wrapper.find('FilterField').map(node => {
    return node.prop('filter')
  })[0]
  expect(actualFilter).toEqual(expectedFilter)
}

export function expectNodes(wrapper, expectedNodes) {
  const actualNodes = wrapper.find('BrowserNode').map(node => {
    const text = node.prop('node').text
    const selected = node.prop('node').selected
    const level = node.prop('level')
    return {
      text,
      level,
      selected
    }
  })
  expect(actualNodes).toMatchObject(expectedNodes)
}

export default {
  simulateNodeIconClick,
  simulateFilterChange,
  expectFilter,
  expectNodes
}
