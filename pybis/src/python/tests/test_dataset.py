import json
import random
import re

import pytest
import time

def test_create_delete_dataset(space):
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    o=space.openbis

    dataset = o.new_dataset(
        type   = 'RAW_DATA',
        sample = '/DEFAULT/DEFAULT/DEFAULT',
        files  = ['testfile'],
        props  = {'name': 'some good name', 'notes': 'my notes' }
    )
    
    assert dataset is not None
    assert dataset.permId is None  # object is not saved
    assert dataset.p is not None
    assert dataset.p.name == 'some good name'
    assert dataset.p.notes == 'my notes'

    with pytest.raises(Exception):
        dataset.non_existing_attribute = "invalid attribute"
        assert "attribute does not exist, should fail" is None

    with pytest.raises(Exception):
        dataset.p.non_existing_property = "invalid propery"
        assert "property does not exist, should fail" is None
    
    dataset.save()

    # now there should appear a permId
    assert dataset.permId is not None

    # get it by permId
    dataset_by_permId = o.get_dataset(dataset.permId)
    assert dataset_by_permId is not None

    permId = dataset.permId
    assert dataset is not None
    dataset.delete('dataset creation test on '+timestamp)

    # get by permId should now throw an error
    with pytest.raises(Exception):
        deleted_ds = o.get_dataset(permId)
        
    


