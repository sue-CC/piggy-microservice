#!/bin/bash

echo "Starting MongoDB setup..."

# INIT DUMP EXECUTION
(
if test -n "$INIT_DUMP"; then
    echo "Executing dump file on the piggy database"
    until mongo piggy -u $MONGO_INITDB_ROOT_USERNAME -p $MONGO_INITDB_ROOT_PASSWORD --authenticationDatabase admin $INIT_DUMP; do sleep 5; done
fi
) &

echo "Start MongoDB"
chown -R mongodb /data/db
gosu mongodb mongod "$@" &

# Wait for MongoDB to initialize
sleep 10

echo "Creating MongoDB users"
mongo <<EOF
use admin
db.createUser({
  user: '$MONGO_INITDB_ROOT_USERNAME',
  pwd: '$MONGO_INITDB_ROOT_PASSWORD',
  roles: [ { role: 'userAdminAnyDatabase', db: 'admin' }, 'readWriteAnyDatabase' ]
})

use $MONGO_INITDB_DATABASE
db.createUser({
  user: '$MONGO_INITDB_USERNAME',
  pwd: '$MONGO_INITDB_PASSWORD',
  roles: [ { role: 'readWrite', db: '$MONGO_INITDB_DATABASE' } ]
})
EOF

echo "MongoDB started successfully"
exec gosu mongodb /usr/local/bin/docker-entrypoint.sh "$@"
