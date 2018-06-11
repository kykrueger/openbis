import json
import random
import re

import pytest
import time
import os
from pybis import DataSet
from pybis import Openbis


def test_create_delete_project(space):
    o=space.openbis

    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    project=o.new_project(space=space, code='illegal title contains spaces')
    with pytest.raises(ValueError):
        project.save()
        assert "should not have been created" is None

    project_name = 'project_'+timestamp
    project=o.new_project(space=space, code=project_name)
    project.save()

    project_exists=o.get_project(project_name)
    assert project_exists is not None
    project_exists.delete('test project on '+timestamp)
    
    with pytest.raises(ValueError):
        project_no_longer_exists=o.get_project(project_name)
        assert "project {} should have been deleted".format(project_name) is None
    

def test_create_project_with_attachment(space):
    o=space.openbis

    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    project_name = 'project_'+timestamp
    filename = os.path.join(os.path.dirname(__file__), 'testfile')

    if not os.path.exists(filename):
        raise ValueError("File not found: {}".format(filename))

    project=o.new_project(space=space, code=project_name, attachments=filename)
    assert project.attachments is not None
    project.save()

    project_exists=o.get_project(project_name)
    assert project_exists is not None
    assert project_exists.attachments is not None
    
