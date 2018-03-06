import json
import random
import re

import pytest
import time
from random import randint

def test_crud_tag(openbis_instance):
    tag_name = 'test_tag_{}'.format(randint(0,1000)).upper()
    description='description of tag ' + tag_name

    tag = openbis_instance.new_tag(
        code=tag_name, 
        description=description
    )

    assert tag.code == tag_name
    assert tag.description == description
    assert tag.permId is None

    tag.save()

    assert tag.permId is not None

    tag_exists = openbis_instance.get_tag(tag.permId)
    assert tag_exists is not None

    altered_description = 'altered description of tag ' + tag_name
    tag.description = altered_description
    tag.save()
    assert tag.description == altered_description

    tag.delete('test')
    
    with pytest.raises(ValueError):
        tag_does_not_exists = openbis_instance.get_tag(tag.permId)
        assert "deleted tag should no longer be present" is None

