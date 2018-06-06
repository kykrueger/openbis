import shutil
import os
from .utils import run_shell
from .command_result import CommandException
from .checksum import ChecksumGeneratorCrc32, ChecksumGeneratorGitAnnex


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
            return run_shell([self.git_path, "status", "--porcelain"], strip_leading_whitespace=False)
        else:
            return run_shell([self.git_path, "status", "--porcelain", path], strip_leading_whitespace=False)

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
        return run_shell([self.git_path, "annex", "add", path, "--include-dotfiles"])

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

    def git_ignore(self, path):
        result = run_shell([self.git_path, 'check-ignore', path])
        if result.returncode == 1:
            with open(".gitignore", "a") as gitignore:
                gitignore.write(path)
                gitignore.write("\n")

    def git_delete_if_untracked(self, file):
        result = run_shell([self.git_path, 'ls-files', '--error-unmatch', file])
        if 'did not match' in result.output:
            run_shell(['rm', file])

class GitRepoFileInfo(object):
    """Class that gathers checksums and file lengths for all files in the repo."""

    def __init__(self, git_wrapper):
        self.git_wrapper = git_wrapper

    def contents(self, git_annex_hash_as_checksum=False):
        """Return a list of dicts describing the contents of the repo.
        :return: A list of dictionaries
          {'crc32': checksum,
           'checksum': checksum other than crc32
           'checksumType': type of checksum
           'fileLength': size of the file,
           'path': path relative to repo root.
           'directory': False
          }"""
        files = self.file_list()
        cksum = self.cksum(files, git_annex_hash_as_checksum)
        return cksum

    def file_list(self):
        tree = self.git_wrapper.git_ls_tree()
        if tree.failure():
            return []
        lines = tree.output.split("\n")
        files = [line.split("\t")[-1].strip() for line in lines]
        return files

    def cksum(self, files, git_annex_hash_as_checksum=False):

        if git_annex_hash_as_checksum == False:
            checksum_generator = ChecksumGeneratorCrc32()
        else:
            checksum_generator = ChecksumGeneratorGitAnnex()

        checksums = []

        for file in files:
            checksum = checksum_generator.get_checksum(file)
            checksums.append(checksum)

        return checksums
