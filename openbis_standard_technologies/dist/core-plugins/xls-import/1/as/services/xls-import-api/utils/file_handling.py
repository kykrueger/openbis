class FileHandler(object):

    def __init__(self, scripts):
        self.scripts = scripts

    def get_script(self, script_path):
        print("SSSSSSS")
        print(script_path)
        print(self.scripts)
        return self.scripts[script_path]
