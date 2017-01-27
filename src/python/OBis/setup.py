import os

from setuptools import setup

setup(name='obis',
      version='0.1.0',
      description='Local data management with assistance from OpenBIS.',
      url='https://sissource.ethz.ch/sis/pybis/',
      author='SIS | ID | ETH Zuerich',
      author_email='chandrasekhar.ramakrishnan@id.ethz.ch',
      license='BSD',
      packages=['obis'],
      install_requires=[
          'pytest',
          'pybis',
          'click'
      ],
      entry_points='''
        [console_scripts]
        obis=obis.scripts.cli:main
      ''',
      zip_safe=True)
