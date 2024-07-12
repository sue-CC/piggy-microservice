#!/bin/sh
if [ -z "$DOCKER_ACCOUNT" ]; then
    DOCKER_ACCOUNT=clytze
fi;
docker build --tag=piggy-account-grpc account-service
docker tag piggy-account-grpc $DOCKER_ACCOUNT/piggy-account-grpc:latest
docker push $DOCKER_ACCOUNT/piggy-account-grpc:latest

docker build --tag=piggy-auth-grpc auth-service
docker tag piggy-auth-grpc $DOCKER_ACCOUNT/piggy-auth-grpc:latest
docker push $DOCKER_ACCOUNT/piggy-auth-grpc

docker build --tag=piggy-notification-grpc notification-service
docker tag piggy-notification-grpc $DOCKER_ACCOUNT/piggy-notification-grpc:latest
docker push $DOCKER_ACCOUNT/piggy-notification-grpc

docker build --tag=piggy-registry registry
docker tag piggy-registry $DOCKER_ACCOUNT/piggy-registry:latest
docker push $DOCKER_ACCOUNT/piggy-registry

docker build --tag=piggy-statistics-grpc statistics-service
docker tag piggy-statistics-grpc $DOCKER_ACCOUNT/piggy-statistics-grpc:latest
docker push $DOCKER_ACCOUNT/piggy-statistics-grpc

docker build --tag=piggy-mongodb mongodb
docker tag piggy-mongodb $DOCKER_ACCOUNT/piggy-mongodb:latest
docker push $DOCKER_ACCOUNT/piggy-mongodb
