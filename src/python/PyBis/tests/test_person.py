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


def test_role_assignments(openbis_instance, role_assignment_person):
    person = openbis_instance.get_person(userId=role_assignment_person)

    for role in person.get_roles():
        role.delete('test')

    roles_exist = person.get_roles()
    assert len(roles_exist) == 0

    person.assign_role('ADMIN')
    roles_exist = person.get_roles()
    assert len(roles_exist) == 1

    person.assign_role(role='ADMIN', space='DEFAULT')
    roles_exist = person.get_roles()
    assert len(roles_exist) == 2

    person.revoke_role(role='ADMIN')
    roles_exist = person.get_roles()
    assert len(roles_exist) == 1

    person.revoke_role(role='ADMIN', space='DEFAULT')
    roles_exist = person.get_roles()
    assert len(roles_exist) == 0
