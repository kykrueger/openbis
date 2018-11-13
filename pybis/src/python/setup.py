import os
import sys

if sys.version_info < (3,3):
    sys.exit('Sorry, Python < 3.3 is not supported')

from setuptools import setup, find_packages

with open("README.md", "r") as fh:
    long_description = fh.read()

setup(
    name='PyBIS',
    version= '1.7.3',
    author='Swen Vermeul |  ID SIS | ETH Zürich',
    author_email='swen@ethz.ch',
    description='openBIS connection and interaction, optimized for using with Jupyter',
    long_description=long_description,
    long_description_content_type="text/markdown",
    url='https://sissource.ethz.ch/sispub/openbis/tree/master/pybis',
    packages=find_packages(),
    license='BSD',
    install_requires=[
        'pytest',
        'requests',
        'datetime',
        'pandas',
        'click',
        'texttable',
        'tabulate',
    ],
    python_requires=">=3.3",
    classifiers=[
        "Programming Language :: Python :: 3.3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
)
