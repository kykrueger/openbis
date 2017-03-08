import pytest

from pybis import Openbis


@pytest.yield_fixture(scope="module")
def openbis_instance():
    # instance = Openbis("http://localhost:20000")
    # Test against a real instance
    instance = Openbis("https://localhost:8443", verify_certificates=False)
    print("\nLOGGING IN...")
    instance.login('admin', 'anypassword')
    yield instance
    instance.logout()
    print("LOGGED OUT...")
