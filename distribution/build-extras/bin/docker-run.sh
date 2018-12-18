#!/bin/bash

wget https://github.com/sharmalab/bindaas/releases/download/v3.3.5/bindaas-dist-3.3.5.tar.gz

tar -xvf bindaas-dist-3.3.5.tar.gz && rm bindaas-dist-3.3.5.tar.gz

docker run --name bindaas -v $PWD/bin:/root/bindaas/bin/ -v $PWD/bundles:/root/bindaas/bundles/ -v $PWD/log:/root/bindaas/log/ -p 8080:8080 -p 9099:9099 pradeeban/bindaas:3.3.5