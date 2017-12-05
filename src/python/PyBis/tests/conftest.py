import pytest

from pybis import Openbis

openbis_url = 'https://localhost:8443'
admin_username = 'admin'
admin_password = '*****'

@pytest.yield_fixture(scope="module")
def openbis_instance():
    instance = Openbis(url=openbis_url, verify_certificates=False)
    print("\nLOGGING IN...")
    instance.login(admin_username, admin_password)
    yield instance
    instance.logout()
    print("LOGGED OUT...")
