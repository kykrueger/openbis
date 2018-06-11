#!/usr/bin/env python
# -*- coding: utf-8 -*-

import getpass
from unittest.mock import Mock, MagicMock, ANY

from .openbis_command import OpenbisCommand
from .. import data_mgmt


def test_prepare_run(monkeypatch):
    # given
    dm = data_mgmt.DataMgmt(openbis=Mock())
    openbis_command = OpenbisCommand(dm)
    monkeypatch.setattr(getpass, 'getpass', lambda s: 'password')
    dm.openbis.is_session_active.return_value = False
    # when
    openbis_command.prepare_run()
    # then
    dm.openbis.is_session_active.assert_called()
    dm.openbis.login.assert_called_with('auser', 'password', save_token=True)

# TODO
# def test_determine_hostname():
#     # given
#     dm = data_mgmt.DataMgmt(openbis=Mock())
#     openbis_command = OpenbisCommand(dm)
#     define_config(openbis_command)
#     # when
#     openbis_command.determine_hostname()
#     # then

def define_config(openbis_command):
    openbis_command.config_dict = {
        'hostname': None
    }
    
