#!/usr/bin/env bash
# =============================================================================
# Remindy バックエンド 正常系スモークテスト
#
# 前提:
#   - バックエンドが起動済み。
#   - local プロファイル(= Flyway の db/seed が適用済み)で起動していること。
#     例) SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
#   - シードの alice ユーザー(パスワード devpass = {noop}devpass)と既知IDを使用する。
#
# 使い方:
#   ./smoke-test.sh                 # 基本(認証〜同期まで)
#   RUN_INTAKE=1 ./smoke-test.sh    # 自然言語インテークも実行(GEMINI_API_KEY 必須)
#   BASE=http://192.168.1.10:8080/api/v1 ./smoke-test.sh   # 接続先を変更
# =============================================================================
set -uo pipefail

BASE="${BASE:-http://localhost:8080/api/v1}"
HOST="${BASE%/api/v1}"

# --- シード由来の固定値(R__seed_local_data.sql と一致) ---
SEED_EMAIL="${SEED_EMAIL:-alice@example.com}"
SEED_PASSWORD="${SEED_PASSWORD:-devpass}"
SEED_REMINDER_ID="a0000000-0000-0000-0000-000000000002"   # DAILY(alice 所有)
SEED_STUDY_ID="b0000000-0000-0000-0000-000000000001"      # QA(alice 所有)

# --- 内部状態 ---
TOKEN=""
LAST_BODY=""
PASS=0
FAIL=0
have_jq=0; command -v jq >/dev/null 2>&1 && have_jq=1

GREEN=$'\033[32m'; RED=$'\033[31m'; DIM=$'\033[2m'; RESET=$'\033[0m'

# api METHOD PATH EXPECTED_STATUS [JSON_BODY]
# 実行後、レスポンス本文を LAST_BODY に格納する。
api() {
  local method="$1" path="$2" expect="$3" data="${4:-}"
  local args=(-s -w $'\n%{http_code}' -X "$method" "${BASE}${path}")
  [ -n "$TOKEN" ] && args+=(-H "Authorization: Bearer ${TOKEN}")
  [ -n "$data" ] && args+=(-H 'Content-Type: application/json' -d "$data")

  local resp status
  resp="$(curl "${args[@]}")"
  status="$(printf '%s' "$resp" | tail -n1)"
  LAST_BODY="$(printf '%s' "$resp" | sed '$d')"

  if [ "$status" = "$expect" ]; then
    printf '%sPASS%s [%s] %-6s %s\n' "$GREEN" "$RESET" "$status" "$method" "$path"
    PASS=$((PASS + 1))
  else
    printf '%sFAIL%s [exp %s got %s] %-6s %s\n' "$RED" "$RESET" "$expect" "$status" "$method" "$path"
    printf '%s      %s%s\n' "$DIM" "$LAST_BODY" "$RESET"
    FAIL=$((FAIL + 1))
  fi
}

# JSON からフィールドを取り出す(jq が無ければ sed でフォールバック)
json_field() {
  local field="$1" body="$2"
  if [ "$have_jq" = 1 ]; then
    printf '%s' "$body" | jq -r ".${field}"
  else
    printf '%s' "$body" | sed -E "s/.*\"${field}\":\"([^\"]+)\".*/\1/"
  fi
}

section() { printf '\n%s=== %s ===%s\n' "$DIM" "$1" "$RESET"; }

# health_api METHOD PATH EXPECTED_STATUS (HOST ベース、/api/v1 なし)
health_api() {
  local method="$1" path="$2" expect="$3" extra_args="${4:-}"
  local resp status
  # shellcheck disable=SC2086
  resp="$(curl -s -w $'\n%{http_code}' -X "$method" "${HOST}${path}" $extra_args)"
  status="$(printf '%s' "$resp" | tail -n1)"
  LAST_BODY="$(printf '%s' "$resp" | sed '$d')"

  if [ "$status" = "$expect" ]; then
    printf '%sPASS%s [%s] %-6s %s\n' "$GREEN" "$RESET" "$status" "$method" "${HOST}${path}"
    PASS=$((PASS + 1))
  else
    printf '%sFAIL%s [exp %s got %s] %-6s %s\n' "$RED" "$RESET" "$expect" "$status" "$method" "${HOST}${path}"
    printf '%s      %s%s\n' "$DIM" "$LAST_BODY" "$RESET"
    FAIL=$((FAIL + 1))
  fi
}

# =============================================================================
# 0. ヘルスチェック
# =============================================================================
section "0. Health"

# 0-1 health: 即時チェック
health_api GET /health 200

# 0-2 connect: つながるまで待機(最大310秒)
health_api GET /health/connect 200 "--max-time 310"

# =============================================================================
# 1. 認証
# =============================================================================
section "1. Auth"

# 1-1 register: シードを汚さないよう毎回ユニークなメールで新規登録(happy path)
NEW_EMAIL="smoketest+$(date +%s)@example.com"
api POST /auth/register 201 "{\"emailAddress\":\"${NEW_EMAIL}\",\"password\":\"P@ssw0rd1\"}"

# 1-2 login: 以降で使うトークンはシードの alice で取得
api POST /auth/login 200 "{\"emailAddress\":\"${SEED_EMAIL}\",\"password\":\"${SEED_PASSWORD}\"}"
TOKEN="$(json_field accessToken "$LAST_BODY")"
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  printf '%s致命的: ログインに失敗しトークンを取得できませんでした。local プロファイルでシード投入済みか、alice/devpass が正しいか確認してください。%s\n' "$RED" "$RESET"
  exit 1
fi

# 1-3 me
api GET /auth/me 200

# =============================================================================
# 2. リマインダー
# =============================================================================
section "2. Reminders"

# 参照系: シードデータを利用
api GET /reminders 200
api GET "/reminders/${SEED_REMINDER_ID}" 200

# 破壊系: 一時リソースを作ってそれに対して実行(シードを壊さない)
api POST /reminders 201 '{"title":"[smoke]一時リマインダー","schedule":{"type":"WEEKLY","dayOfWeek":"MONDAY","time":"09:00"}}'
TMP_RID="$(json_field id "$LAST_BODY")"

api PUT   "/reminders/${TMP_RID}" 200 '{"title":"[smoke]更新後","schedule":{"type":"DAILY","time":"08:30"}}'
api PATCH "/reminders/${TMP_RID}" 200 '{"enabled":false}'
api DELETE "/reminders/${TMP_RID}" 204

# =============================================================================
# 3. 学習項目
# =============================================================================
section "3. Study items"

api GET /study-items 200
api GET "/study-items/${SEED_STUDY_ID}" 200

api POST /study-items 201 '{"kind":"TERM","prompt":"[smoke]冪等性","answer":"何度実行しても結果が変わらない"}'
TMP_SID="$(json_field id "$LAST_BODY")"

api PUT   "/study-items/${TMP_SID}" 200 '{"kind":"QA","prompt":"[smoke]HTTP 200 は？","answer":"成功"}'
api PATCH "/study-items/${TMP_SID}" 200 '{"enabled":false}'
api DELETE "/study-items/${TMP_SID}" 204

# =============================================================================
# 4. 学習通知設定
# =============================================================================
section "4. Notification setting"

api GET /study/notification-setting 200
api PUT /study/notification-setting 200 '{"frequency":"FIVE_TIMES","enabled":true}'
# シードの状態(THREE_TIMES)へ戻す
api PUT /study/notification-setting 200 '{"frequency":"THREE_TIMES","enabled":true}'

# =============================================================================
# 5. 同期
# =============================================================================
section "5. Sync"
api GET /sync 200

# =============================================================================
# 6. 自然言語インテーク(任意 / GEMINI_API_KEY が必要)
# =============================================================================
if [ "${RUN_INTAKE:-0}" = "1" ]; then
  section "6. Intake (LLM)"
  api POST /reminders/intake 200 '{"text":"明日の9時に資料を送る","clientTime":"2026-07-12T20:00:00+09:00"}'
  SESSION="$(json_field sessionId "$LAST_BODY")"
  if [ -n "$SESSION" ] && [ "$SESSION" != "null" ]; then
    api POST "/reminders/intake/${SESSION}/messages" 200 '{"text":"タイトルは会議準備にして"}'
  fi
else
  printf '\n%s(インテークAPIは RUN_INTAKE=1 かつ GEMINI_API_KEY 設定時のみ実行)%s\n' "$DIM" "$RESET"
fi

# =============================================================================
# 結果
# =============================================================================
printf '\n%s============================%s\n' "$DIM" "$RESET"
printf 'PASS: %s%d%s   FAIL: %s%d%s\n' "$GREEN" "$PASS" "$RESET" "$RED" "$FAIL" "$RESET"

[ "$FAIL" -eq 0 ] && exit 0 || exit 1