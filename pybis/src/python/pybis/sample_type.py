from .property import PropertyAssignments
from .semantic_annotation import SemanticAnnotation

class SampleType(PropertyAssignments):
    """ Helper class for sample types, adding functionality.
    """

    def new_semantic_annotation(self, **kwargs):
        return SemanticAnnotation(
            openbis_obj=self.openbis, isNew=True, 
            entityType=self.code, **kwargs
        )

    def get_semantic_annotations(self):
        return self.openbis.search_semantic_annotations(entityType=self.code)
