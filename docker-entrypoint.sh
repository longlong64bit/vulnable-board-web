#!/bin/sh
set -e
HOST="${MYSQL_HOST:-mysql}"
PORT="${MYSQL_PORT:-3306}"
echo "Waiting for MySQL at $HOST:$PORT..."
for i in $(seq 1 30); do
  if nc -z "$HOST" "$PORT" 2>/dev/null; then
    echo "MySQL is ready."
    break
  fi
  echo "Attempt $i: MySQL not ready, waiting 2s..."
  sleep 2
done
exec "$@"
