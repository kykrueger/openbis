import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_create_person(openbis_instance):
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    person_name = 'test_person_' + timestamp
    person = openbis_instance.new_person(userId=person_name)
    person.save()
    person_exists = openbis_instance.get_person(userId=person_name)
    assert person_exists is not None

    person.delete("test on {}".format(timestamp))

    with pytest.raises(ValueError):
        person_not_exists = openbis_instance.get_person(userId=person_name)
        assert person_not_exists is None


def test_role_assignments(openbis_instance):
    person = openbis_instance.get_person(userId='admin')

    with pytest.raises(ValueError):
        roles_not_exist = group.get_roles()
        assert roles_not_exist is None

    group.assign_role('ADMIN')
    roles_exist = group.get_roles()
    assert roles_exist is not None

    group.revoke_role('ADMIN')
    with pytest.raises(ValueError):
        roles_not_exist = group.get_roles()
        assert roles_not_exist is None
        
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    group.delete("text on {}".format(timestamp))
