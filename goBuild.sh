#!/bin/sh -x

docker run --user `id -u`:`id -g` --env AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -v $PWD:/build --entrypoint /build/sbt ubirch/java clean compile test