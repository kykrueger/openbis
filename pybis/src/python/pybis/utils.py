from datetime import datetime
from pathlib import Path
import re

# display messages when in a interactive context (IPython or Jupyter)
try:
    get_ipython()
except Exception:
    VERBOSE = False
else:
    VERBOSE = True

def parse_jackson(input_json):
    """openBIS uses a library called «jackson» to automatically generate the JSON RPC output.
       Objects that are found the first time are added an attribute «@id».
       Any further findings only carry this reference id.
       This function is used to dereference the output.
    """
    interesting=['tags', 'registrator', 'modifier', 'owner', 'type', 'parents', 
        'children', 'containers', 'container', 'properties', 'experiment', 'sample',
        'project', 'space', 'propertyType', 'entityType', 'propertyType', 'propertyAssignment',
        'externalDms', 'roleAssignments', 'user', 'users', 'authorizationGroup', 'vocabulary',
        'validationPlugin', 'dataSetPermId', 'dataStore'
    ]
    found = {} 
    def build_cache(graph):
        if isinstance(graph, list):
            for item in graph:
                build_cache(item)
        elif isinstance(graph, dict) and len(graph) > 0:
            for key, value in graph.items():
                if key in interesting:
                    if isinstance(value, dict):
                        if '@id' in value:
                            found[value['@id']] = value
                        build_cache(value)
                    elif isinstance(value, list):
                        for item in value:
                            if isinstance(item, dict):
                                if '@id' in item:
                                    found[item['@id']] = item
                                build_cache(item)
                elif isinstance(value, dict):
                    build_cache(value)
                elif isinstance(value, list):
                    build_cache(value)
                    
    def deref_graph(graph):            
        if isinstance(graph, list):
            for i, list_item in enumerate(graph):
                if isinstance(list_item, int):
                    try:
                        graph[i] = found[list_item]
                    except KeyError:
                        pass
                else:
                    deref_graph(list_item)
        elif isinstance(graph, dict) and len(graph) > 0:
            for key, value in graph.items():
                if key in interesting:
                    if isinstance(value, dict):
                        deref_graph(value)
                    elif isinstance(value, int):
                        graph[key] = found[value]
                    elif isinstance(value, list):
                        for i, list_item in enumerate(value):
                            if isinstance(list_item, int):
                                if list_item in found:
                                    value[i] = found[list_item]
                                else:
                                    value[i] = list_item
                elif isinstance(value, dict):
                    deref_graph(value)
                elif isinstance(value, list):
                    deref_graph(value)

    build_cache(input_json)
    deref_graph(found)
    deref_graph(input_json)


def check_datatype(type_name, value):
    if type_name == 'INTEGER':
        return isinstance(value, int)
    if type_name == 'BOOLEAN':
        return isinstance(value, bool)
    if type_name == 'VARCHAR':
        return isinstance(value, str)
    return True


def split_identifier(ident):
    bla = []
    bla=ident.upper().split("/")
    results = {}
    try:
        if bla[0] == '':
            bla.pop(0)
        if bla[-1] == '':
            bla.pop(-1)
        results["space"] = bla.pop(0)
        results["code"]  = bla.pop(-1)
        results["experiment"] = bla.pop(0)
    except Exception:
        pass

    return results


def format_timestamp(ts):
    if ts is None:
        return ''
    if ts != ts:  # test for NaN
        return ''
    return datetime.fromtimestamp(round(ts/1000)).strftime('%Y-%m-%d %H:%M:%S')


def is_identifier(ident):
    # assume we got a sample identifier e.g. /TEST/TEST-SAMPLE
    match = re.search('/', ident)
    if match:
        return True
    else:
        return False


def is_permid(ident):
    match = re.match('^\d+\-\d+$', ident)
    if match:
        return True
    else:
        return False


def nvl(val, string=''):
    if val is None:
        return string
    return val

def extract_permid(permid):
    if not isinstance(permid, dict):
        return str(permid)
    return permid['permId']

def extract_code(obj):
    if not isinstance(obj, dict):
        return '' if obj is None else str(obj)
    return '' if obj['code'] is None else obj['code']

def extract_downloadUrl(obj):
    if not isinstance(obj, dict):
        return '' if obj is None else str(obj)
    return '' if obj['downloadUrl'] is None else obj['downloadUrl']

def extract_name(obj):
    if not isinstance(obj, dict):
        return '' if obj is None else str(obj)
    return '' if obj['name'] is None else obj['name']

def extract_deletion(obj):
    del_objs = []
    for deleted_object in obj['deletedObjects']:
        del_objs.append({
            "reason": obj['reason'],
            "permId": deleted_object["id"]["permId"],
            "type": deleted_object["id"]["@type"]
        })
    return del_objs


def extract_attr(attr):
    def attr_func(obj):
        if isinstance(obj, dict):
            return obj.get(attr, '')
        else:
            return str(obj)
    return attr_func


def extract_identifier(ident):
    if not isinstance(ident, dict):
        return str(ident)
    return ident['identifier']


def extract_nested_identifier(ident):
    if not isinstance(ident, dict):
        return str(ident)
    return ident['identifier']['identifier']


def extract_nested_permid(permid):
    if not isinstance(permid, dict):
        return '' if permid is None else str(permid)
    return '' if permid['permId']['permId'] is None else permid['permId']['permId'] 
    
def extract_nested_permids(items):
    if not isinstance(items, list):
        return []

    return list(item['permId']['permId'] for item in items)


def extract_property_assignments(pas):
    pa_strings = []
    for pa in pas:
        if not isinstance(pa['propertyType'], dict):
            pa_strings.append(pa['propertyType'])
        else:
            pa_strings.append(pa['propertyType']['label'])
    return pa_strings


def extract_role_assignments(ras):
    ra_strings = []
    for ra in ras:
        ra_strings.append({
            "role": ra['role'],
            "roleLevel": ra['roleLevel'],
            "space": ra['space']['code'] if ra['space'] else None
        })
    return ra_strings


def extract_person(person):
    if not isinstance(person, dict):
        if person is None:
            return ''
        else:
            return str(person)
    return person['userId']

def extract_person_details(person):
    if not isinstance(person, dict):
        return str(person)
    return "{} {} <{}>".format(
        person['firstName'],
        person['lastName'],
        person['email']
    )

def extract_id(id):
    if not isinstance(id, dict):
        return str(id)
    else:
        return id['techId']

def extract_userId(user):
    if isinstance(user, list):
        return ", ".join([
            u['userId'] for u in user
        ])
    elif isinstance(user, dict):
        return user['userId']
    else:
        return str(user)


def check_symlink(target_dir: str) -> Path:
    # there are several options: basically resolvable symlink, non-resolvable symlink, does not exist at all or exists but it is not a symlink

    target_dir_path = Path(target_dir)

    if target_dir_path.is_symlink() and target_dir_path.exists():
        return target_dir_path.resolve()
    elif target_dir_path.is_symlink() and not target_dir_path.exists():
        raise FileNotFoundError(
            f"target_dir={target_dir} is non-resolvable symlink. No such file or directory: {target_dir_path.resolve()}"
        )
    elif not target_dir_path.is_symlink() and not target_dir_path.exists():
        raise FileNotFoundError(f"target_dir={target_dir} does not exist")
    else:
        raise ValueError(f"target_dir={target_dir} exists but it is not a symlink")


def create_symlink(
    target_dir: str, source_dir: str, mountpoint: str, replace_if_symlink: bool = True
):
    # Path(mountpoint,ds.experiment.identifier[1:],ds.permId)
    # replace_if_symlink will replace the the target_dir in case it is an existing symlink

    mountpoint_path = Path(mountpoint)

    # check mountpoint
    if not mountpoint_path.is_mount():
        if not mountpoint_path.exists():
            raise FileNotFoundError(f"mountpoint={mountpoint} does not exist")
        else:
            raise ValueError(f"mountpoint={mountpoint} exists but it is not a mount")

    # join mountpoint and source_dir
    # if source_dir is absolute we have to remove "/" to be able to join it with mountpoint
    # I did not find any way to achieve this in case they are pathlib.Path
    if source_dir[0] == "/":
        source_dir = source_dir[1:]
    source_dir_path = Path(mountpoint, source_dir)

    target_dir_path = Path(target_dir)
    if target_dir_path.is_symlink() and replace_if_symlink:
        target_dir_path.unlink()

    target_dir_path.symlink_to(source_dir_path, target_is_directory=True)