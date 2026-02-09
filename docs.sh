#!/bin/zsh
# docs.sh - QUANTAF Documentation (Docusaurus)
#
# Usage:
#   ./docs.sh                  # Install deps + dev server on http://localhost:3000
#   ./docs.sh serve            # Install deps + dev server on http://localhost:3000
#   ./docs.sh build            # Build static site to ./documentation/build
#   ./docs.sh preview          # Build + serve production build on http://localhost:3000
#   ./docs.sh stop             # Stop the running docs server
#   ./docs.sh clean            # Clear Docusaurus cache + build artifacts
#   ./docs.sh help             # Show this help message

set -euo pipefail

# ─── Configuration ─────────────────────────────────────────────────
DOCS_DIR="documentation"
PORT=3000
PID_FILE=".docs_server.pid"

# ─── Colors ────────────────────────────────────────────────────────
BOLD='\033[1m'
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
DIM='\033[2m'
NC='\033[0m'

function info()    { echo "${BLUE}ℹ${NC} $1"; }
function success() { echo "${GREEN}✔${NC} $1"; }
function warn()    { echo "${YELLOW}⚠${NC} $1"; }
function error()   { echo "${RED}✘${NC} $1"; }

# ═══════════════════════════════════════════════════════════════════
#  UTILITIES
# ═══════════════════════════════════════════════════════════════════

function kill_port() {
  local pids=$(lsof -ti tcp:$PORT 2>/dev/null || true)
  if [[ -n $pids ]]; then
    warn "Port $PORT is in use. Killing process(es): $pids"
    echo "$pids" | xargs kill -9 2>/dev/null || true
    sleep 1
  fi
}

function stop_server() {
  if [[ -f $PID_FILE ]]; then
    local PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
      info "Stopping documentation server (PID: $PID)..."
      kill "$PID" 2>/dev/null || true
      sleep 1
    fi
    rm -f "$PID_FILE"
  fi
  kill_port
  success "Documentation server stopped."
}

function check_node() {
  if ! command -v node >/dev/null 2>&1; then
    error "Node.js is not installed."
    info "Install via: ${BOLD}brew install node${NC}   (or https://nodejs.org)"
    exit 1
  fi

  local NODE_VERSION=$(node -v | sed 's/v//' | cut -d. -f1)
  if (( NODE_VERSION < 18 )); then
    error "Node.js >= 18 is required. Found: $(node -v)"
    info "Update via: ${BOLD}brew upgrade node${NC}"
    exit 1
  fi
}

function install_deps() {
  check_node

  if [[ ! -d "$DOCS_DIR/node_modules" ]]; then
    info "Installing documentation dependencies..."
    (cd "$DOCS_DIR" && npm install --silent)
    success "Dependencies installed."
  else
    success "Dependencies already installed."
  fi
}

# ═══════════════════════════════════════════════════════════════════
#  COMMANDS
# ═══════════════════════════════════════════════════════════════════

function cmd_serve() {
  echo ""
  echo "${BOLD}${CYAN}━━━ QUANTAF Documentation ━━━${NC}"
  echo ""
  install_deps
  kill_port
  echo ""
  info "Starting documentation dev server..."
  (cd "$DOCS_DIR" && npm start) &
  echo $! > $PID_FILE
  sleep 4
  echo ""
  success "Documentation server running."
  echo ""
  echo "  ${BOLD}Open:${NC}  ${CYAN}http://localhost:$PORT${NC}"
  echo "  ${BOLD}Stop:${NC}  ./docs.sh stop"
  echo ""
  wait
}

function cmd_build() {
  echo ""
  echo "${BOLD}${CYAN}━━━ QUANTAF Documentation — Build ━━━${NC}"
  echo ""
  install_deps
  info "Building static site..."
  (cd "$DOCS_DIR" && npm run build)
  echo ""
  success "Static site built → ${CYAN}$DOCS_DIR/build/${NC}"
  echo ""
  echo "  ${DIM}To preview: ./docs.sh preview${NC}"
  echo ""
}

function cmd_preview() {
  echo ""
  echo "${BOLD}${CYAN}━━━ QUANTAF Documentation — Preview ━━━${NC}"
  echo ""
  install_deps

  if [[ ! -d "$DOCS_DIR/build" ]]; then
    warn "No build directory found. Building first..."
    (cd "$DOCS_DIR" && npm run build)
  fi

  kill_port
  echo ""
  info "Serving production build..."
  (cd "$DOCS_DIR" && npm run serve) &
  echo $! > $PID_FILE
  sleep 2
  echo ""
  success "Production preview running."
  echo ""
  echo "  ${BOLD}Open:${NC}  ${CYAN}http://localhost:$PORT${NC}"
  echo "  ${BOLD}Stop:${NC}  ./docs.sh stop"
  echo ""
  wait
}

function cmd_stop() {
  echo ""
  echo "${BOLD}${RED}━━━ Stopping Documentation Server ━━━${NC}"
  echo ""
  stop_server
  echo ""
}

function cmd_clean() {
  echo ""
  echo "${BOLD}${YELLOW}━━━ Cleaning Documentation Cache ━━━${NC}"
  echo ""
  (cd "$DOCS_DIR" && npx docusaurus clear 2>/dev/null || true)
  rm -rf "$DOCS_DIR/build" "$DOCS_DIR/.docusaurus"
  success "Cache and build artifacts cleared."
  echo ""
}

function cmd_help() {
  echo ""
  echo "${BOLD}QUANTAF Documentation${NC} ${DIM}(Docusaurus)${NC}"
  echo ""
  echo "${BOLD}Usage:${NC} ./docs.sh [command]"
  echo ""
  echo "${BOLD}Commands:${NC}"
  echo "  ${BOLD}serve${NC}     Install deps + start dev server on port $PORT ${DIM}(default)${NC}"
  echo "  ${BOLD}build${NC}     Build static site to ./$DOCS_DIR/build"
  echo "  ${BOLD}preview${NC}   Build + serve production build on port $PORT"
  echo "  ${BOLD}stop${NC}      Stop the running docs server"
  echo "  ${BOLD}clean${NC}     Clear Docusaurus cache and build artifacts"
  echo "  ${BOLD}help${NC}      Show this help message"
  echo ""
  echo "${BOLD}Examples:${NC}"
  echo "  ./docs.sh              ${DIM}# Start dev server (hot-reload)${NC}"
  echo "  ./docs.sh build        ${DIM}# Build for production (GitHub Pages)${NC}"
  echo "  ./docs.sh preview      ${DIM}# Preview production build locally${NC}"
  echo "  ./docs.sh stop         ${DIM}# Stop server${NC}"
  echo ""
  echo "${BOLD}CI/CD:${NC}"
  echo "  The GitHub Actions workflow (.github/workflows/docs.yml) builds and"
  echo "  deploys the documentation to GitHub Pages automatically on push to main."
  echo ""
}

# ═══════════════════════════════════════════════════════════════════
#  MAIN — Command Router
# ═══════════════════════════════════════════════════════════════════

case "${1:-}" in
  serve)        cmd_serve ;;
  build)        cmd_build ;;
  preview)      cmd_preview ;;
  stop)         cmd_stop ;;
  clean)        cmd_clean ;;
  help|--help|-h) cmd_help ;;
  "")           cmd_serve ;;
  *)
    error "Unknown command: $1"
    cmd_help
    exit 1
    ;;
esac
