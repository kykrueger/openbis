import os

from setuptools import setup

data_dir = os.path.join('man','man1')
data_files = [(d, [os.path.join(d,f) for f in files])
    for d, folders, files in os.walk(data_dir)]

setup(name='obis',
      version='0.1.0',
      description='Local data management with assistance from OpenBIS.',
      url='https://sissource.ethz.ch/sis/pybis/',
      author='SIS | ID | ETH Zuerich',
      author_email='chandrasekhar.ramakrishnan@id.ethz.ch',
      license='BSD',
      packages=['obis', 'obis.dm', 'obis.dm.commands', 'obis.scripts'],
      data_files=data_files,
      package_data={'obis' : ['dm/git-annex-attributes']},
      install_requires=[
          'pyOpenSSL',
          'pytest',
          'pybis',
          'click'
      ],
      entry_points='''
        [console_scripts]
        obis=obis.scripts.cli:main
      ''',
      zip_safe=False)
