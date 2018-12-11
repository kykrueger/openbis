#!/usr/bin/python
# encoding=utf8
#!/usr/bin/python
#
# Requirement:
#   The pybis module must be available.

import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis()
        self.installPybis()
        openbisController = self.createOpenbisController()
        openbisController.createTestDatabase("openbis")
        openbisController.allUp()

        openbis = self._get_openbis()
        self._test_login(openbis)
        self._test_server_information(openbis)
        self._test_logout(openbis)

    def _get_openbis(self):
        import pybis
        return pybis.Openbis(url="https://localhost:8443", verify_certificates=False)

    def _test_login(self, openbis):
        openbis.login('admin', 'admin', save_token=True)
        assert openbis.is_session_active() == True

    def _test_server_information(self, openbis):
        server_information = openbis.get_server_information()
        assert type(server_information.get_major_version()) is int
        assert type(server_information.get_minor_version()) is int

    def _test_logout(self, openbis):
        openbis.logout()
        assert openbis.is_session_active() == False

TestCase(settings, __file__).runTest()
