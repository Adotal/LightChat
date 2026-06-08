#!/usr/bin/env bash
# Aplica ChatServer/schema.sql a la base de datos definida en ChatServer/.env
# usando el driver MySQL incluido en lib/ (no requiere el cliente 'mysql').
#
# Uso:  ./apply-schema.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CP="$(ls "$ROOT"/lib/*.jar | tr '\n' ':')"

cd "$ROOT/ChatServer"

if [ ! -f .env ]; then
  echo "[ERROR] No existe ChatServer/.env"; exit 1
fi

echo "[INFO] Compilando aplicador..."
javac -cp "$CP" -d /tmp/applyschema tools/ApplySchema.java

echo "[INFO] Aplicando schema.sql..."
java -cp "$CP:/tmp/applyschema" ApplySchema

echo "[INFO] Esquema aplicado correctamente."
