from tabulate import tabulate
from texttable import Texttable
from pybis.utils import check_datatype, split_identifier, format_timestamp, is_identifier, is_permid, nvl

class PropertyHolder():

    def __init__(self, openbis_obj, type=None):
        self.__dict__['_openbis'] = openbis_obj
        self.__dict__['_property_names'] = []
        if type is not None:
            self.__dict__['_type'] = type
            for prop in type.data['propertyAssignments']:
                self._property_names.append(prop['propertyType']['code'].lower())

    def _get_terms(self, vocabulary):
        return self._openbis.get_terms(vocabulary)

    def _all_props(self):
        props = {}
        for code in self._type.codes():
            props[code] = getattr(self, code)
        return props

    def all(self):
        props = {}
        for code in self._type.codes():
            props[code] = getattr(self, code)
        return props

    def all_nonempty(self):
        props = {}
        for code in self._type.codes():
            value = getattr(self, code)
            if value is not None:
                props[code] = value

        return props

    def __getattr__(self, name):
        """ attribute syntax can be found out by
            adding an underscore at the end of the property name
        """ 
        if name.endswith('_'):
            name = name.rstrip('_')
            if name in self._type.prop:
                property_type = self._type.prop[name]['propertyType']
                if property_type['dataType'] == 'CONTROLLEDVOCABULARY':
                    return self._get_terms(property_type['vocabulary']['code'])
                else:
                    syntax = { property_type["label"] : property_type["dataType"]}
                    if property_type["dataType"] == "TIMESTAMP":
                        syntax['syntax'] = 'YYYY-MM-DD HH:MIN:SS'
                    return syntax
            return None


    def __setattr__(self, name, value):
        if name not in self._property_names:
            raise KeyError("No such property: '{}'. Allowed properties are: {}".format(name, self._property_names)) 
        property_type = self._type.prop[name]['propertyType']
        data_type = property_type['dataType']
        if data_type == 'CONTROLLEDVOCABULARY':
            voc = self._get_terms(property_type['vocabulary']['code'])
            value = str(value).upper()
            if value not in voc.df['code'].values:
                raise ValueError("Value for attribute {} must be one of these terms: {}".format(
                    name, ", ".join(voc.df['code'].values)
                ))
        elif data_type in ('INTEGER', 'BOOLEAN', 'VARCHAR'):
            if not check_datatype(data_type, value):
                raise ValueError("Value must be of type {}".format(data_type))
        self.__dict__[name] = value

    def __dir__(self):
        return self._property_names

    def _repr_html_(self):
        def nvl(val, string=''):
            if val is None:
                return string
            elif val == 'true':
                return True
            elif val == 'false':
                return False
            return val
        html = """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>property</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """

        for prop in self._property_names:
            html += "<tr> <td>{}</td> <td>{}</td> </tr>".format(
                prop, nvl(getattr(self, prop, ''),'')
            )

        html += """
            </tbody>
            </table>
        """
        return html

    def __repr__(self):
        def nvl(val, string=''):
            if val is None:
                return string
            elif val == 'true':
                return True
            elif val == 'false':
                return False
            return str(val)

        headers = ['property', 'value']

        lines = []
        for prop_name in self._property_names:
            lines.append([
                prop_name,
                nvl(getattr(self, prop_name, ''))
            ])
        return tabulate(lines, headers=headers)

