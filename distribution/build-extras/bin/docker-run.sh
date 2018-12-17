#!/bin/bash

mkdir bindaas

cd bindaas

wget https://github.com/sharmalab/bindaas/releases/download/v3.3.2/bindaas-dist-3.3.2.tar.gz

tar -xvf bindaas-dist-3.3.2.tar.gz && rm bindaas-dist-3.3.2.tar.gz

docker stop bindaas-public

docker rm bindaas-public

docker run --name bindaas-public -v bin:/root/bindaas/bin/ -v log:/root/bindaas/log/ -p 8080:8080 -p 9099:9099 pradeeban/bindaas:3.3.2