#!/bin/bash
pip install /home/ubuntu/RESTAggregator-1.0.0-py2-none-any.whl 
nohup waitress-serve --host="" --port=5001 --call "RESTAggregator:create_app" &

