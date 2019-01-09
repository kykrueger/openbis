import json
import random
import re

import pytest
import time

def test_create_delete_dataset(space):
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    o=space.openbis

    dataset_type = 'UNKNOWN'
    dataset = o.new_dataset(
        type='UNKNOWN',
        sample='/DEFAULT/DEFAULT/DEFAULT',
        files=['testfile']
    )

    assert dataset is not None
    assert dataset.permId is None
    dataset.save()

    # now there should appear a permId
    assert dataset.permId is not None

    # get it by permId
    dataset_by_permId = o.get_dataset(dataset.permId)
    assert dataset_by_permId is not None

    permId = dataset.permId
    dataset.delete('dataset creation test on '+timestamp)
#
#    # get by permId should now throw an error
#    with pytest.raises(ValueError):
#        dataset.by_permId = o.get_dataset(permId)
#        
    


