from setuptools import find_packages, setup
from os import path
from io import open


setup(
    name='RESTAggregator',  #project name
    version='1.0.0',
    py_modules=["RESTAggregator"],
    python_requires='>=2.7',
    include_package_data=True,
    zip_safe=False,
    install_requires=[
        'flask',
        'flask-mysqldb',
    ],
)
