import os

from setuptools import setup

setup(name='elnjupyter',
      version='0.1.0',
      description='A webservice to create jupyter notebooks in the users home directory',
      url='https://sissource.ethz.ch/sis/pybis/',
      author='SIS | ID | ETH Zuerich',
      author_email='swen@ethz.ch',
      license='BSD',
      packages=['pybis'],
      install_requires=[
          'tornado',
          'ssl',
      ],
      zip_safe=True)
