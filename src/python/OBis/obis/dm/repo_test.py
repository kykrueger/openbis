#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
repo_test.py


Created by Chandrasekhar Ramakrishnan on 2017-03-03.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

from . import repo as dm_repo
from . import data_mgmt
from .data_mgmt_test import git_status, copy_test_data


def test_data_use_case(tmpdir):
    tmp_dir_path = str(tmpdir)
    assert git_status(tmp_dir_path).returncode == 128  # The folder should not be a git repo at first.

    repo = dm_repo.DataRepo(tmp_dir_path)

    result = repo.init("test")
    assert result.returncode == 0

    assert git_status(tmp_dir_path).returncode == 0  # The folder should be a git repo now
    assert git_status(tmp_dir_path, annex=True).returncode == 0  # ...and a git-annex repo as well.

    copy_test_data(tmpdir)

    result = repo.commit("Added data.")
    assert result.returncode == 0

    with data_mgmt.cd(tmp_dir_path):
        # The zip should be in the annex
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'snb-data.zip'])
        present_p = result.output.split('\n')[-1]
        assert present_p == 'present: true'

        # The txt files should be in git normally
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'text-data.txt'])
        present_p = result.output.split(' ')[-1]
        assert present_p == 'failed'
        result = data_mgmt.run_shell(['git', 'log', '--oneline', 'text-data.txt'])
        present_p = " ".join(result.output.split(' ')[1:])
        assert present_p == 'Added data.'
