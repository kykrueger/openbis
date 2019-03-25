import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_create_delete_experiment(space):
    o=space.openbis
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    new_code='test_experiment_'+timestamp

    with pytest.raises(TypeError):
        # experiments must be assigned to a project
        e_new = o.new_experiment(
            code=new_code,
            type='DEFAULT_EXPERIMENT',
        )

    e_new = o.new_experiment(
        code=new_code,
        project='DEFAULT',
        type='DEFAULT_EXPERIMENT',
    )
    assert e_new.project is not None
    assert e_new.permId is None

    e_new.save()

    assert e_new.permId is not None
    assert e_new.code == new_code.upper()
    assert e_new.identifier == '/DEFAULT/DEFAULT/'+new_code.upper()

    e_exists = o.get_experiment('/DEFAULT/DEFAULT/'+new_code.upper())
    assert e_exists is not None

    e_new.delete('delete test experiment '+new_code.upper())

    with pytest.raises(ValueError):
        e_no_longer_exists = o.get_experiment('/DEFAULT/DEFAULT/'+new_code.upper())


def test_get_experiments(space):
    # test paging
    o=space.openbis
    current_datasets = o.get_experiments(start_with=1, count=1)
    assert current_datasets is not None
    # we cannot assert == 1, because search is delayed due to lucene search...
    assert len(current_datasets) <= 1

