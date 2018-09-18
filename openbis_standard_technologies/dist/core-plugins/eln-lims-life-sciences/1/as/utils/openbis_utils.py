from utils.file_handling import get_filename_from_path


def is_internal_namespace(property_value):
    return property_value.startswith(u'$')


def get_script_name_for(owner_code, script_path):
        return owner_code + '.' + get_filename_from_path(script_path)
