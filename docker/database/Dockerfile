FROM postgres:10-alpine

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d nfdiv_scheduler -U $NFDIV_SCHEDULER_DB_USER_NAME

EXPOSE 5432
