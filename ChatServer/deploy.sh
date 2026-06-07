#!/usr/bin/env bash
# Script de actualización del ChatServer en la VM de Azure.
# Uso: ./deploy.sh   (ejecútalo desde la carpeta ChatServer/)
#
# Hace: git pull -> recompila el .jar con ant -> reinicia el servicio systemd.
set -euo pipefail

# Ir a la raíz del repo (un nivel arriba de ChatServer/)
cd "$(dirname "$0")/.."

echo "==> Trayendo últimos cambios de GitHub..."
git pull

echo "==> Recompilando ChatServer..."
cd ChatServer
# El proyecto NetBeans espera una plataforma llamada JDK_21; le pasamos la ruta.
ant -Dplatforms.JDK_21.home=/usr/lib/jvm/java-21-openjdk-amd64 clean jar

echo "==> Reiniciando el servicio..."
sudo systemctl restart chatserver

echo "==> Listo. Estado del servicio:"
sudo systemctl status chatserver --no-pager -l | head -n 15
