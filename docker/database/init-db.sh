#!/usr/bin/env bash

set -e

if [ -z "NFDIV_SCHEDULER_DB_USER_NAME" ] || [ -z "NFDIV_SCHEDULER_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'NFDIV_SCHEDULER_DB_USER_NAME' and 'NFDIV_SCHEDULER_DB_PASSWORD'."
  exit 1
fi
echo "Runnning the filesdocker"
echo $NFDIV_SCHEDULER_DB_PASSWORD
echo $NFDIV_SCHEDULER_DB_USER_NAME

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$NFDIV_SCHEDULER_DB_USER_NAME --set PASSWORD=$NFDIV_SCHEDULER_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';

  CREATE DATABASE nfdiv_scheduler
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
