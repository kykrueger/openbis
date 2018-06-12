import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis

def test_create_delete_sample(space):
    o=space.openbis

    sample_type = 'UNKNOWN'
    sample = o.new_sample(code='illegal sample name with spaces', type=sample_type, space=space)
    with pytest.raises(ValueError):
        sample.save()
        assert "should not have been created" is None

    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    sample_code = 'test_sample_'+timestamp
    sample = o.new_sample(code=sample_code, type=sample_type, space=space)
    assert sample is not None
    assert sample.space == space
    assert sample.code == sample_code
    
    assert sample.permId is None
    sample.save()

    # now there should appear a permId
    assert sample.permId is not None

    # get it by permId
    sample_by_permId = o.get_sample(sample.permId)
    assert sample_by_permId is not None

    sample_by_permId = space.get_sample(sample.permId)
    assert sample_by_permId is not None


    # get it by identifier
    sample_by_identifier = o.get_sample(sample.identifier)
    assert sample_by_identifier is not None

    sample_by_identifier = space.get_sample(sample.identifier)
    assert sample_by_identifier is not None

    sample.delete('sample creation test on '+timestamp)

def test_create_delete_space_sample(space):
    o=space.openbis
    sample_type = 'UNKNOWN'
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    sample_code = 'test_sample_'+timestamp

    sample = space.new_sample(code=sample_code, type=sample_type)
    assert sample is not None
    assert sample.space == space
    assert sample.code == sample_code
    sample.save()
    assert sample.permId is not None
    sample.delete('sample space creation test on '+timestamp)

def test_parent_child(space):
    o=space.openbis
    sample_type = 'UNKNOWN'
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    parent_code = 'parent_sample_{}'.format(timestamp)
    sample_parent = o.new_sample(code=parent_code, type=sample_type, space=space)
    sample_parent.save()

    child_code='child_sample_{}'.format(timestamp)
    sample_child =  o.new_sample(code=child_code, type=sample_type, space=space, parent=sample_parent)
    sample_child.save()
    time.sleep(5)

    ex_sample_parents = sample_child.get_parents()
    ex_sample_parent = ex_sample_parents[0]
    assert ex_sample_parent.identifier == '/DEFAULT/{}'.format(parent_code).upper()

    ex_sample_children = ex_sample_parent.get_children()
    ex_sample_child = ex_sample_children[0]
    assert ex_sample_child.identifier == '/DEFAULT/{}'.format(child_code).upper()


