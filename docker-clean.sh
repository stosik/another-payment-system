#!/bin/sh

set -x

docker-compose down
docker stop alpine/soca
docker rm -f $(docker ps -a -q)