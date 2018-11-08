import os
import sys
from collections import deque
from java.nio.file import Files, Paths
from java.io import File
from java.util import HashMap
from java.util import ArrayList

TYPES_FOLDER = "%s/life-sciences-types/" % [p for p in sys.path if p.find('core-plugins') >= 0][0]
SCRIPTS = os.path.join(TYPES_FOLDER, 'scripts')


def get_all_scripts():
    scripts = HashMap()
    for rel_path, script in list_all_files(SCRIPTS):
        scripts.put(rel_path, script)

    return scripts


def list_xls_byte_arrays():
    xls = ArrayList()
    for f in os.listdir(TYPES_FOLDER):
        if f.endswith('.xls') or f.endswith('.xlsx'):
            excel_file = open(os.path.join(TYPES_FOLDER, f))
            xls.add(excel_file.read())
            excel_file.close()
    return xls


def list_all_files(source_root_path):
    todo = []
    todo.append(File(source_root_path))
    while todo:
        f = todo.pop()
        if f.isDirectory():
            new_files = f.listFiles()
            if new_files is not None:
                todo.extend(f.listFiles())
            continue
        if f.isFile():
            source_file = f.getAbsolutePath()
            script_file = open(source_file)
            script = script_file.read()
            script_file.close()
            file_path = source_file.replace(source_root_path, "")
            if file_path.startswith("/"):
                file_path = file_path[1:]
            yield file_path, script
