from .property import PropertyHolder
from .attribute import AttrHolder
from .utils import VERBOSE

class OpenBisObject():

    def __init_subclass__(
        cls,
        entity=None
    ):
        """create a specialized parent class.
        The class that inherits from OpenBisObject does not need
        to implement its own __init__ method in order to provide the
        entity name. Instead, it can pass the entity name as a param:
        class XYZ(OpenBisObject, entity="myEntity")
        """
        cls._entity=entity

    def __init__(self, openbis_obj, entity=None, type=None, data=None, props=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, self._entity, type)

        # existing OpenBIS object
        if data is not None:
            self._set_data(data)

        if props is not None:
            for key in props:
                setattr(self.p, key, props[key])

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__['data'] = data

        # put the properties in the self.p namespace (without checking them)
        if 'properties' in data:
            for key, value in data['properties'].items():
                self.p.__dict__[key.lower()] = value
            
        # object is already saved to openBIS, so it is not new anymore
        self.a.__dict__['_is_new'] = False

    @property
    def attrs(self):
        return self.__dict__['a']

    @property
    def space(self):
        try:
            return self.openbis.get_space(self._space['permId'])
        except Exception:
            pass

    @property
    def project(self):
        try:
            return self.openbis.get_project(self._project['identifier'])
        except Exception:
            pass

    @property
    def experiment(self):
        try:
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass
    collection = experiment  # Alias

    @property
    def sample(self):
        try:
            return self.openbis.get_sample(self._sample['identifier'])
        except Exception:
            pass
    object = sample # Alias

    def __getattr__(self, name):
        return getattr(self.__dict__['a'], name)

    def __setattr__(self, name, value):
        if name in ['set_properties', 'set_tags', 'add_tags']:
            raise ValueError("These are methods which should not be overwritten")
        setattr(self.__dict__['a'], name, value)

    def _repr_html_(self):
        """Print all the assigned attributes (identifier, tags, etc.) in a nicely formatted table. See
        AttributeHolder class.
        """
        return self.a._repr_html_()

    def __repr__(self):
        """same thing as _repr_html_() but for IPython
        """
        return self.a.__repr__()


    def delete(self, reason):
        """Delete this openbis entity.
        A reason is mandatory to delete any entity.
        """
        if not self.data:
            return

        self.openbis.delete_openbis_entity(
            entity=self._entity,
            objectId=self.data['permId'],
            reason=reason
        )
        if VERBOSE: print(
            "{} {} successfully deleted.".format(
                self._entity,
                self.permId
            )
        )
