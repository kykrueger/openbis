import json
import random
import re

import pytest
import time

def test_create_delete_dataset(space):
    o=space.openbis

    dataset_type = 'UNKNOWN'
    dataset = o.new_dataset(
        type='UNKNOWN',
        sample='/DEFAULT/DEFAULT',
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

    dataset_by_permId = space.get_dataset(dataset.permId)
    assert dataset_by_permId is not None


    # get it by identifier
    dataset_by_identifier = o.get_dataset(dataset.identifier)
    assert dataset_by_identifier is not None

    dataset_by_identifier = space.get_dataset(dataset.identifier)
    assert dataset_by_identifier is not None

    dataset.delete('dataset creation test on '+timestamp)


