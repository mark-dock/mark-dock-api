#!/bin/bash
cd server_code
git config --global --add safe.directory /home/ubuntu/server_code
git fetch origin main
git reset --hard origin/main
if ! sudo systemctl is-active --quiet docker; then
  echo "Docker is not running. Starting Docker..."
  sudo systemctl start docker
fi
sudo docker compose -f compose.prod.yaml down --remove-orphans
sudo docker compose -f compose.prod.yaml up --build -d
