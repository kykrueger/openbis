import shutil
import os
from .utils import run_shell


class GitWrapper(object):
    """A wrapper on commands to git."""

    def __init__(self, git_path=None, git_annex_path=None, find_git=None):
        self.git_path = git_path
        self.git_annex_path = git_annex_path

    def can_run(self):
        """Return true if the perquisites are satisfied to run"""
        if self.git_path is None:
            return False
        if self.git_annex_path is None:
            return False
        if run_shell([self.git_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        if run_shell([self.git_annex_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        return True

    def git_init(self, path):
        return run_shell([self.git_path, "init", path])

    def git_status(self, path=None):
        if path is None:
            return run_shell([self.git_path, "status", "--porcelain"])
        else:
            return run_shell([self.git_path, "status", "--porcelain", path])

    def git_annex_init(self, path, desc):
        cmd = [self.git_path, "-C", path, "annex", "init", "--version=6"]
        if desc is not None:
            cmd.append(desc)
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "config", "annex.thin", "true"]
        result = run_shell(cmd)
        if result.failure():
            return result

        attributes_src = os.path.join(os.path.dirname(__file__), "git-annex-attributes")
        attributes_dst = os.path.join(path, ".gitattributes")
        shutil.copyfile(attributes_src, attributes_dst)
        cmd = [self.git_path, "-C", path, "add", ".gitattributes"]
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "commit", "-m", "Initial commit."]
        result = run_shell(cmd)
        return result

    def git_add(self, path):
        return run_shell([self.git_path, "add", path])

    def git_commit(self, msg):
        return run_shell([self.git_path, "commit", '-m', msg])

    def git_top_level_path(self):
        return run_shell([self.git_path, 'rev-parse', '--show-toplevel'])

    def git_commit_hash(self):
        return run_shell([self.git_path, 'rev-parse', '--short', 'HEAD'])

    def git_ls_tree(self):
        return run_shell([self.git_path, 'ls-tree', '--full-tree', '-r', 'HEAD'])

    def git_checkout(self, path):
        return run_shell([self.git_path, "checkout", path])

    def git_reset_to(self, commit_hash):
        return run_shell([self.git_path, 'reset', commit_hash])


class GitRepoFileInfo(object):
    """Class that gathers checksums and file lengths for all files in the repo."""

    def __init__(self, git_wrapper):
        self.git_wrapper = git_wrapper

    def contents(self):
        """Return a list of dicts describing the contents of the repo.
        :return: A list of dictionaries
          {'crc32': checksum,
           'fileLength': size of the file,
           'path': path relative to repo root.
           'directory': False
          }"""
        files = self.file_list()
        cksum = self.cksum(files)
        return cksum

    def file_list(self):
        tree = self.git_wrapper.git_ls_tree()
        if tree.failure():
            return []
        lines = tree.output.split("\n")
        files = [line.split("\t")[-1].strip() for line in lines]
        return files

    def cksum(self, files):
        cmd = ['cksum']
        cmd.extend(files)
        result = run_shell(cmd)
        if result.failure():
            return []
        lines = result.output.split("\n")
        return [self.checksum_line_to_dict(line) for line in lines]

    @staticmethod
    def checksum_line_to_dict(line):
        fields = line.split(" ")
        return {
            'crc32': int(fields[0]),
            'fileLength': int(fields[1]),
            'path': fields[2]
        }