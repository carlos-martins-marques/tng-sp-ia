#!/bin/bash
set -e
cd vim-wrapper-ovs/
docker rm -fv $(docker ps -a -f name=vim-wrapper-ovs -q)
docker rmi $(docker images -f reference=vim-wrapper-ovs* -q)

docker build -t registry.sonata-nfv.eu:5000/vim-wrapper-ovs .
