from ..dm.command_result import CommandResult


def set_property(data_mgmt, resolver, prop, value, is_global, is_data_set_property=False):
    """Helper function to implement the property setting semantics."""
    loc = 'global' if is_global else 'local'
    try:
        if is_data_set_property:
            resolver.set_value_for_json_parameter('properties', prop, value, loc, apply_rules=True)
        else:
            resolver.set_value_for_parameter(prop, value, loc, apply_rules=True)
    except ValueError as e:
        if data_mgmt.debug ==  True:
            raise e
        return CommandResult(returncode=-1, output="Error: " + str(e))
    if not is_global:
        return data_mgmt.commit_metadata_updates(prop)
    else:
        return CommandResult(returncode=0, output="")
