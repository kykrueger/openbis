import pytest
import time

from pybis import Openbis

openbis_url = 'https://localhost:8443'
admin_username = 'admin'
admin_password = 'changeit'

@pytest.yield_fixture(scope="module")
def openbis_instance():
    instance = Openbis(
        url=openbis_url, 
        verify_certificates=False, 
        allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True
    )
    print("\nLOGGING IN...")
    instance.login(admin_username, admin_password)
    yield instance
    instance.logout()
    print("LOGGED OUT...")


@pytest.yield_fixture(scope="module")
def space():
    o = Openbis(
        url=openbis_url, 
        verify_certificates=False, 
        allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True
    )
    o.login(admin_username, admin_password)

    # create a space
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    space_name = 'test_space_' + timestamp
    space = o.new_space(code=space_name)
    space.save()
    space_exists = o.get_space(code=space_name)
    yield space_exists

    # teardown
    o.logout()
