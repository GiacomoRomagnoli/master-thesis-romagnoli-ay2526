#!/usr/bin/env bash
set -e  # stop on error

# Directory dello script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> 1. Running experiment..."
(
  cd "$SCRIPT_DIR/jakta-exp" || exit 1
  chmod +x run_experiments.sh
  ./run_experiments.sh
)

echo "==> 2. Running evaluation..."
(
  cd "$SCRIPT_DIR/jakta-evals" || exit 1
  chmod +x evaluation.sh
  ./evaluation.sh
)

echo "==> 3. Running Gradle reports..."
(
  cd "$SCRIPT_DIR" || exit 1
  ./gradlew :jakta-reports:run
)

echo "==> DONE!"
