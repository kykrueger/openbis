import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_create_delete_project(space):
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
    e_new.save()

    assert e_new.permId is not None
    assert e_new.code == new_code
    assert e_identifier == '/DEFAULT/DEFAULT/'+new_code


    #project=o.new_project(space=space, code='illegal title contains spaces')
    #with pytest.raises(ValueError):
    #    project.save()
    #    assert "should not have been created" is None

    #project_name = 'project_'+timestamp
    #project=o.new_project(space=space, code=project_name)
    #project.save()

    #project_exists=o.get_project(project_name)
    #assert project_exists is not None
    #project_exists.delete('test project on '+timestamp)
    #
    #with pytest.raises(ValueError):
    #    project_no_longer_exists=o.get_project(project_name)
    #    assert "project {} should have been deleted".format(project_name) is None
