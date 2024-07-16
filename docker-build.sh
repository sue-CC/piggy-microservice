#!/bin/sh
if [ -z "$DOCKER_ACCOUNT" ]; then
    DOCKER_ACCOUNT=clytze
fi;
docker build --tag=piggy-account-rest account-service
docker tag piggy-account-rest $DOCKER_ACCOUNT/piggy-account-rest:latest
docker push $DOCKER_ACCOUNT/piggy-account-rest:latest

docker build --tag=piggy-auth-rest auth-service
docker tag piggy-auth-rest $DOCKER_ACCOUNT/piggy-auth-rest:latest
docker push $DOCKER_ACCOUNT/piggy-auth-rest

docker build --tag=piggy-notification-rest notification-service
docker tag piggy-notification-rest $DOCKER_ACCOUNT/piggy-notification-rest:latest
docker push $DOCKER_ACCOUNT/piggy-notification-rest

docker build --tag=piggy-registry registry
docker tag piggy-registry $DOCKER_ACCOUNT/piggy-registry:latest
docker push $DOCKER_ACCOUNT/piggy-registry

docker build --tag=piggy-statistics-rest statistics-service
docker tag piggy-statistics-rest $DOCKER_ACCOUNT/piggy-statistics-rest:latest
docker push $DOCKER_ACCOUNT/piggy-statistics-rest

docker build --tag=piggy-mongodb mongodb
docker tag piggy-mongodb $DOCKER_ACCOUNT/piggy-mongodb:latest
docker push $DOCKER_ACCOUNT/piggy-mongodb
