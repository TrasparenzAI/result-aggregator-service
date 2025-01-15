#!/bin/bash

# Result Aggregator Service for Linux installation script
#
# See https://github.com/cnr-anac/result-service/ for more details.
#
# This script is meant for quick & easy install via:
#   $ curl -fsSL https://raw.githubusercontent.com/cnr-anac/result-aggregator-service/main/first-setup.sh -o first-setup.sh && sh first-setup.sh

# NOTE: Make sure to verify the contents of the script
#       you downloaded matches the contents of first-setup.sh
#       located at https://github.com/cnr-anac/result-aggregator-service/first-setup.sh
#
# This script need docker and docker compose plugin to be installed successfully.

INSTALL_DIR=${INSTALL_DIR:-.}

echo "Result aggregator service installation script."
echo "Before running this script install docker, docker compose and add current user to docker group." 
read -p "Press enter to continue, Ctrl+C to abort" ready
command -v docker -v >/dev/null 2>&1 || { echo >&2 "docker not found.  Aborting."; exit 1; }
command -v docker compose version >/dev/null 2>&1 || { echo >&2 "docker compose plugin not found.  Aborting."; exit 1; }

mkdir -p $INSTALL_DIR/postgres-data
cd $INSTALL_DIR

curl https://raw.githubusercontent.com/cnr-anac/result-aggregator-service/main/docker-compose.yml -o docker-compose.yml
curl https://raw.githubusercontent.com/cnr-anac/result-aggregator-service/main/.env -o .env

# Creazione e impostazione della password di accesso al database
DB_PASSWORD=`tr -dc A-Za-z0-9 < /dev/urandom | head -c 16 ; echo`
sed "s|DB_PASSWORD=|DB_PASSWORD=${DB_PASSWORD}|g" -i .env

# Avvio del postgres e creazione del DB vuoto configse
docker compose up -d postgres
# Attesa che il container docker sia pronto
sleep 10
docker compose exec postgres createdb -U cnr-anac aggregated-results

# Avvio del config-service che si occuperà anche di popolare il db 
docker compose up -d
