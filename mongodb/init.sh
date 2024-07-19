#!/bin/bash

# Remove authentication-related logic and database renaming
echo "Starting MongoDB setup..."

# INIT DUMP EXECUTION
(
if test -n "$INIT_DUMP"; then
    echo "Executing dump file on the piggy database"
    until mongo "$DB_NAME" "$INIT_DUMP"; do sleep 5; done
fi
) &

echo "Start MongoDB"
chown -R mongodb /data/db
gosu mongodb mongod "$@"

# Wait for MongoDB to initialize
sleep 5

echo "MongoDB started successfully"
exec gosu mongodb /usr/local/bin/docker-entrypoint.sh "$@"