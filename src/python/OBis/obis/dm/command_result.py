class CommandResult(object):
    """Encapsulate result from a subprocess call."""

    def __init__(self, completed_process=None, returncode=None, output=None):
        """Convert a completed_process object into a ShellResult."""
        if completed_process:
            self.returncode = completed_process.returncode
            self.output = completed_process.stdout.decode('utf-8').strip()
        else:
            self.returncode = returncode
            self.output = output

    def __str__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def __repr__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def success(self):
        return self.returncode == 0

    def failure(self):
        return not self.success()