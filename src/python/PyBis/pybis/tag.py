from .openbis_object import OpenBisObject 
from .utils import VERBOSE

class Tag(OpenBisObject):
    """ 
    """

    def __init__(self, openbis_obj, type, project=None, data=None, props=None, code=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Experiment', type)

        if data is not None:
            self._set_data(data)

        if project is not None:
            setattr(self, 'project', project)

        if props is not None:
            for key in props:
                setattr(self.p, key, props[key])

        if code is not None:
            self.code = code

        if kwargs is not None:
            defs = _definitions('Experiment')
            for key in kwargs:
                if key in defs['attrs']:
                    setattr(self, key, kwargs[key])
                else:
                    raise ValueError("{attr} is not a known attribute for an Experiment".format(attr=key))


