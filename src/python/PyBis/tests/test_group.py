import json
import random
import re

import pytest
import time
from random import randint
from pybis import DataSet
from pybis import Openbis


def test_crud_group(openbis_instance):
    group_name = 'test_group_{}'.format(randint(0,1000)).upper()
    group = openbis_instance.new_group(code=group_name, description='description of group ' + group_name)
    group.save()
    group_exists = openbis_instance.get_group(group_name)
    assert group_exists is not None

    changed_description = 'changed description of group '+group_name
    group.description = changed_description
    group.save()
    group_changed = openbis_instance.get_group(code=group_name)
    assert group_changed.description == changed_description

    group.delete("test")
    with pytest.raises(ValueError):
        group_not_exists = openbis_instance.get_group(code=group_name)
        assert group_not_exists is None


def test_role_assignments(openbis_instance):
    group_name = 'test_group_{}'.format(randint(0,1000)).upper()
    group = openbis_instance.new_group(
        code=group_name, 
        description='description of group ' + group_name
    )
    group.save()

    roles_not_exist = group.get_roles()
    assert len(roles_not_exist) == 0

    group.assign_role('ADMIN')
    roles_exist = group.get_roles()
    assert len(roles_exist) == 1

    group.revoke_role('ADMIN')
    roles_not_exist = group.get_roles()
    assert len(roles_not_exist) == 0
        
    group.delete("test")
    
