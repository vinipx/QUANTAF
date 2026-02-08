#!/bin/zsh
# docs.sh - Manage QUANTAF documentation site

DOCS_DIR="docs"
PORT=8000
PID_FILE=".docs_server.pid"

function check_pipx() {
  if ! command -v pipx >/dev/null 2>&1; then
    echo "pipx not found. Installing via Homebrew..."
    brew install pipx || { echo "Failed to install pipx. Please install Homebrew and pipx manually."; exit 1; }
    pipx ensurepath
  fi
}

function check_mkdocs_material() {
  # Check if mkdocs-material is installed in mkdocs pipx environment
  if ! pipx list | grep -q 'mkdocs-material'; then
    echo "Injecting mkdocs-material into mkdocs environment..."
    pipx inject mkdocs mkdocs-material || { echo "Failed to inject mkdocs-material. Please check pipx and Python setup."; exit 1; }
  fi
}

function check_mkdocs() {
  if ! command -v mkdocs >/dev/null 2>&1; then
    echo "mkdocs not found. Installing via pipx..."
    check_pipx
    pipx install mkdocs || { echo "Failed to install mkdocs. Please check pipx and Python setup."; exit 1; }
  fi
  check_mkdocs_material
}

function install_requirements() {
  check_mkdocs
}

function kill_port() {
  local port=$1
  local pids=$(lsof -ti tcp:$port)
  if [[ -n $pids ]]; then
    echo "Port $port is in use. Killing process(es): $pids"
    kill $pids
  fi
}

function serve_docs() {
  kill_port $PORT
  echo "Serving documentation locally on http://localhost:$PORT"
  mkdocs serve --dev-addr=0.0.0.0:$PORT &
  echo $! > $PID_FILE
  echo "Docs server started. PID: $(cat $PID_FILE)"
}

function stop_server() {
  kill_port $PORT
  if [[ -f $PID_FILE ]]; then
    PID=$(cat $PID_FILE)
    echo "Stopping docs server (PID: $PID)..."
    kill $PID && rm $PID_FILE
    echo "Docs server stopped and port $PORT released."
  else
    echo "No docs server running. Port $PORT released if occupied."
  fi
}

case "$1" in
  install)
    install_requirements
    ;;
  serve)
    install_requirements
    serve_docs
    ;;
  stop)
    stop_server
    ;;
  ""|*)
    # If no argument, install and serve automatically
    install_requirements
    serve_docs
    ;;
esac
