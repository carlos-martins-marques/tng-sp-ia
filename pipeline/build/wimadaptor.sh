#!/bin/bash
set -e
docker build -t registry.sonata-nfv.eu:5000/wim-adaptor -f wim-adaptor/Dockerfile .
