#!/bin/sh

set -x

# Exposing docker deamon to the container we will run app in.
docker run -d -p 2375:2375 -v /var/run/docker.sock:/var/run/docker.sock alpine/socat tcp-listen:2375,reuseaddr,fork unix-connect:/var/run/docker.sock

# Create a new image version with latest code changes.
docker compose up -d