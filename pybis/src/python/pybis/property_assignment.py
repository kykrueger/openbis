from tabulate import tabulate
from texttable import Texttable
from pybis.utils import check_datatype, split_identifier, format_timestamp, is_identifier, is_permid, nvl
from pandas import DataFrame


class PropertyAssignments():
    """ holds all properties that are assigned to an entity (eg. sample or experiment)
    """

    def __init__(self, openbis_obj, data):
        self.openbis = openbis_obj
        self.data = data
        self.prop = {}
        if self.data['propertyAssignments'] is None:
            self.data['propertyAssignments'] = []
        for pa in self.data['propertyAssignments']:
            self.prop[pa['propertyType']['code'].lower()] = pa

    def __str__(self):
        """String representation of this entity type
        """
        return self.data['code']

    def _attrs(self):
        return ['code', 'description', 'autoGeneratedCode', 'subcodeUnique',
            'generatedCodePrefix', 'listable', 'showContainer', 'showParents',
            'showParentMetadata', 'validationPlugin']

    def __dir__(self):
        return self._attrs()

    def __getattr__(self, name):
        if name in self._attrs():
            if name in self.data:
                return self.data[name]
            else:
                return ''

    def __eq__(self, other):
        return str(self) == str(other)

    def __ne__(self, other):
        return str(self) != str(other)

    def get_propertyAssignments(self):
        attrs = ['code', 'dataType', 'description', 'label', 'mandatory', 'ordinal']
        pas = [ {**pa['propertyType'], **pa} for pa in self.data['propertyAssignments'] ]
        return DataFrame(pas, columns=attrs)

    def codes(self):
        codes = []
        for pa in self.data['propertyAssignments']:
            codes.append(pa['propertyType']['code'].lower())
        return codes


    def _repr_html_(self):
        def nvl(val, string=''):
            if val is None:
                return string
            return val

        html = "<p>{}: <b>{}</b>".format(
            self.data['@type'].split('.')[-1],
            self.data['code'], 
        )

        html += """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>attribute</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """

        for attr in self._attrs():
            if attr == 'validationPlugin':
                continue
            html += "<tr> <td>{}</td> <td>{}</td> </tr>".format(
                attr, nvl(getattr(self, attr, ''), '')
            )

        html += """
            </tbody>
            </table>
        """

        if self.validationPlugin:
            html += "<p/><b>Validation Plugin</b>"
            html += """
                <table border="1" class="dataframe">
                <thead>
                    <tr style="text-align: right;">
                    <th>attribute</th>
                    <th>value</th>
                    </tr>
                </thead>
                <tbody>
            """
            for attr in ['name', 'description', 'pluginType', 'pluginKind',
                'available', 'entityKinds']:
                html += "<tr> <td>{}</td> <td>{}</td> </tr>".format(
                    attr, self.validationPlugin.get(attr)
                )
            html += """
                </tbody>
                </table>
            """

        return html


    def __repr__(self):
        title = """
{}: {}
description: {}""".format (
            self.data['@type'].split('.')[-1],
            self.data['code'], 
            self.data['description']
        )

        table = Texttable()
        table.set_deco(Texttable.HEADER)

        headers = ['code', 'label', 'description', 'dataType', 'mandatory']

        lines = []
        lines.append(headers)
        for pa in self.data['propertyAssignments']:
            lines.append([
                pa['propertyType']['code'].lower(),    
                pa['propertyType']['label'],    
                pa['propertyType']['description'],    
                pa['propertyType']['dataType'],    
                pa['mandatory']
            ])
        table.add_rows(lines)
        table.set_cols_width([28,28,28,28,9])
        table.set_cols_align(['l','l','l','l','l'])
        return title + "\n\n" + table.draw()

