#!/bin/sh
if [ -z "$DOCKER_ACCOUNT" ]; then
    DOCKER_ACCOUNT=clytze
fi;

# Create and use a new builder instance
docker buildx create --name mybuilder --use
docker buildx inspect --bootstrap

# Build and push for multiple architectures
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-account-rest:latest --push account-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-auth-rest:latest --push auth-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-notification-rest:latest --push notification-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-registry:latest --push registry
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-statistics-rest:latest --push statistics-service
docker buildx build --platform linux/amd64,linux/arm64 --tag $DOCKER_ACCOUNT/piggy-mongodb:latest --push mongodb

# Remove the builder instance
docker buildx rm mybuilder

