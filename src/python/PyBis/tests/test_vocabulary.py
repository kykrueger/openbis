import json
import random
import re

import pytest
import time


def test_create_delete_vocabulay_terms(openbis_instance):
    o=openbis_instance 

    terms = o.get_terms()
    assert terms is not None
    assert terms.df is not None
    



