#!/usr/bin/env bash
set -euo pipefail
which java
java -version
######################################
# CONFIGURAZIONE
######################################
JAR_PATH="build/libs/jakta-exp-all.jar"
DEFAULT_INSTANCES=5
ENV_FILE="../.env"
######################################

# Controlla che il file .env esista
if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERRORE] File $ENV_FILE non trovato. Crea un file .env con la variabile API_KEY."
  exit 1
fi

# Legge API_KEY direttamente dal file .env
API_KEY=$(grep -E '^API_KEY=' "$ENV_FILE" | cut -d '=' -f2- | xargs)

if [[ -z "$API_KEY" ]]; then
  echo "[ERRORE] API_KEY non trovata nel file .env"
  exit 1
fi

echo "[INFO] API_KEY letta da .env: ${API_KEY:0:4}****"

# Numero di istanze da parametro o default
INSTANCES="${1:-$DEFAULT_INSTANCES}"

echo "====================================="
echo " Avvio di $INSTANCES esperimenti"
echo " Jar: $JAR_PATH"
echo "====================================="

# Avvia le istanze in parallelo
for ((i=1; i<=INSTANCES; i++)); do
  echo "[INFO] Avvio esperimento $i..."

  API_KEY="$API_KEY" java -jar "$JAR_PATH" \
    --log-to-file false \
    --log-to-console false \
    --lm-server-url "https://openrouter.ai/api/v1/" \
    --model-id "deepseek/deepseek-chat-v3.1:free" \
    --temperature 0.1 &

  sleep 1
done

echo "Tutti gli esperimenti sono stati avviati."
echo "In attesa del completamento..."

wait

echo "Tutti gli esperimenti completati."
