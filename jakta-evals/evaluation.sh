#!/usr/bin/env bash
set -euo pipefail

######################################
# CONFIGURAZIONE
######################################

# Path di default
DEFAULT_BASE_DIR="../jakta-exp/logs/"
JAR_PATH="build/libs/jakta-evals-all.jar"

######################################
# LETTURA ARGOMENTO
######################################
BASE_DIR="${1:-$DEFAULT_BASE_DIR}"

# Normalizzazione: se manca lo slash finale lo aggiunge
[[ "${BASE_DIR}" != */ ]] && BASE_DIR="${BASE_DIR}/"

######################################
# CONTROLLO
######################################
if [[ ! -d "$BASE_DIR" ]]; then
  echo "[ERRORE] La directory '$BASE_DIR' non esiste."
  exit 1
fi

if [[ ! -f "$JAR_PATH" ]]; then
  echo "[ERRORE] JAR non trovato al path: $JAR_PATH"
  exit 1
fi

######################################
# ESECUZIONE
######################################
echo "Esecuzione esperimenti nella directory: $BASE_DIR"
echo

count=0
for subdir in "$BASE_DIR"*/ ; do
  [[ -d "$subdir" ]] || continue
  count=$((count + 1))

  echo "[$count] Valutazione esperimento: $subdir"
  java -jar "$JAR_PATH" --run-dir "$subdir"
  echo "Completato."
  echo
done

echo "Eseguite $count valutazioni totali."
