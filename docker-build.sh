#!/bin/sh
if [ -z "$DOCKER_ACCOUNT" ]; then
    DOCKER_ACCOUNT=clytze
fi;
docker build --tag=piggy-account account-service
docker tag piggy-account $DOCKER_ACCOUNT/piggy-account:latest
docker push $DOCKER_ACCOUNT/piggy-account:latest

docker build --tag=piggy-auth auth-service
docker tag piggy-auth $DOCKER_ACCOUNT/piggy-auth:latest
docker push $DOCKER_ACCOUNT/piggy-auth

docker build --tag=piggy-notification notification-service
docker tag piggy-notification $DOCKER_ACCOUNT/piggy-notification:latest
docker push $DOCKER_ACCOUNT/piggy-notification

docker build --tag=piggy-registry registry
docker tag piggy-registry $DOCKER_ACCOUNT/piggy-registry:latest
docker push $DOCKER_ACCOUNT/piggy-registry

docker build --tag=piggy-statistics statistics-service
docker tag piggy-statistics $DOCKER_ACCOUNT/piggy-statistics:latest
docker push $DOCKER_ACCOUNT/piggy-statistics

docker build --tag=piggy-mongodb mongodb
docker tag piggy-mongodb $DOCKER_ACCOUNT/piggy-mongodb:latest
docker push $DOCKER_ACCOUNT/piggy-mongodb
