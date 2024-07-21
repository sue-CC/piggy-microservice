#!/bin/sh
if [ -z "$DOCKER_ACCOUNT" ]; then
    DOCKER_ACCOUNT=clytze
fi;

# Create and use a new builder instance
docker buildx create --name mybuilder --use
docker buildx inspect --bootstrap

# Build and push for multiple architectures
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-account-grpc:latest --push account-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-auth-grpc:latest --push auth-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-notification-grpc:latest --push notification-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-registry:latest --push registry
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-statistics-grpc:latest --push statistics-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-mongodb:latest --push mongodb

# Remove the builder instance
docker buildx rm mybuilder


##!/bin/sh
#if [ -z "$DOCKER_ACCOUNT" ]; then
#    DOCKER_ACCOUNT=clytze
#fi;
#docker build --tag=piggy-account-grpc account-service
#docker tag piggy-account-grpc $DOCKER_ACCOUNT/piggy-account-grpc:latest
#docker push $DOCKER_ACCOUNT/piggy-account-grpc:latest
#
#docker build --tag=piggy-auth-grpc auth-service
#docker tag piggy-auth-grpc $DOCKER_ACCOUNT/piggy-auth-grpc:latest
#docker push $DOCKER_ACCOUNT/piggy-auth-grpc
#
#docker build --tag=piggy-notification-grpc notification-service
#docker tag piggy-notification-grpc $DOCKER_ACCOUNT/piggy-notification-grpc:latest
#docker push $DOCKER_ACCOUNT/piggy-notification-grpc
#
#docker build --tag=piggy-registry registry
#docker tag piggy-registry $DOCKER_ACCOUNT/piggy-registry:latest
#docker push $DOCKER_ACCOUNT/piggy-registry
#
#docker build --tag=piggy-statistics-grpc statistics-service
#docker tag piggy-statistics-grpc $DOCKER_ACCOUNT/piggy-statistics-grpc:latest
#docker push $DOCKER_ACCOUNT/piggy-statistics-grpc
#
#docker build --tag=piggy-mongodb mongodb
#docker tag piggy-mongodb $DOCKER_ACCOUNT/piggy-mongodb:latest
#docker push $DOCKER_ACCOUNT/piggy-mongodb
