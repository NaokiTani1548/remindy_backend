# Remindy

リマインダーと単語学習を管理する REST API バックエンドサーバー。自然言語でリマインダーを登録できる LLM 連携インテーク機能を備える。

通知の発火・スケジュール計算・学習項目のランダム選出はモバイル端末の責務であり、本 API はデータの CRUD・同期・自然言語の構造化のみを担う。

## 技術スタック

| 項目 | 内容 |
|---|---|
| 言語 | Kotlin 2.3 |
| フレームワーク | Spring Boot 4.1 |
| データベース | PostgreSQL 18 |
| マイグレーション | Flyway |
| 認証 | JWT（HS256、14日有効） |
| LLM | Google Gemini 3.5 Flash |
| Java バージョン | 25 |

---

## 開発環境構築

### 前提条件

- Java 25
- Docker（PostgreSQL を Docker で起動する場合）
- [direnv](https://direnv.net/)

### 1. 環境変数の設定（direnv）

本プロジェクトは [direnv](https://direnv.net/) を使って環境変数を管理する。プロジェクトルートに `.envrc` を作成し、以下の変数を設定する。

```zsh
# .envrc
export GEMINI_API_KEY="your-gemini-api-key"
export JWT_SECRET="your-secret-key-must-be-32-bytes-or-more"
```

#### direnv のセットアップ手順

direnv が未インストールの場合はインストールする。

```zsh
# macOS
brew install direnv
```

シェルに direnv フックを追加する（`~/.zshrc` に追記）。

```zsh
eval "$(direnv hook zsh)"
```

`.envrc` 作成後、以下のコマンドで読み込みを許可する。

```zsh
direnv allow
```

以降はプロジェクトディレクトリに入ると自動的に環境変数が読み込まれる。

#### 環境変数の説明

| 変数名 | 必須 | 説明 |
|---|---|---|
| `GEMINI_API_KEY` | 自然言語インテーク利用時のみ | Google Gemini API キー |
| `JWT_SECRET` | 必須 | JWT 署名キー（32バイト以上推奨） |
| `DB_PASSWORD` | 任意 | DB パスワード（デフォルト: `reminder`） |

`GEMINI_API_KEY` を設定しない場合、自然言語インテーク機能は利用不可となるが、その他の機能は動作する。

### 2. データベースの起動

Docker Compose で PostgreSQL を起動する。

```zsh
docker compose up -d postgres
```

コンテナが起動するとアプリケーション初回起動時に Flyway が自動でスキーマを作成する。

### 3. アプリケーションの起動

```zsh
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

`local` プロファイルを指定するとシードデータが投入され、開発に便利な初期データが利用できる。

起動後、`http://localhost:8080/api/v1` がベース URL となる。

### 4. テスト

```zsh
# ユニット・統合テスト（Testcontainers で PostgreSQL を自動起動）
./gradlew test

# API 結合テスト（アプリ起動済みの状態で実行）
chmod +x scripts/api-test.sh   # 初回のみ
./scripts/api-test.sh

# 自然言語インテークのテストを含める場合
RUN_INTAKE=1 ./scripts/api-test.sh
```

---

## 機能一覧

### 1. ユーザー認証

メールアドレスとパスワードによるアカウント管理。

- **アカウント登録**: メールアドレス（小文字正規化）とパスワード（BCrypt ハッシュ化）で登録
- **ログイン**: JWT アクセストークンを発行（有効期限 14 日）
- **現在のユーザー取得**: トークンから認証済みユーザー情報を返す

認証が必要な全エンドポイントは `Authorization: Bearer <token>` ヘッダが必須。他ユーザーのリソースへのアクセスは存在を秘匿するため `404` を返す。

---

### 2. リマインダー管理

リマインダーの CRUD 管理。スケジュールは以下の4種から選択する。

| スケジュール種別 | 説明 | 必須フィールド |
|---|---|---|
| `ONE_TIME` | 特定の日時に1回のみ | `date`（YYYY-MM-DD）、`time`（HH:mm） |
| `DAILY` | 毎日指定時刻 | `time` |
| `WEEKLY` | 毎週指定曜日・時刻 | `dayOfWeek`（MONDAY〜SUNDAY）、`time` |
| `MONTHLY` | 毎月指定日・時刻 | `dayOfMonth`（1〜31）、`time` |

時刻はすべて JST（+09:00）固定。

**リマインダーリソースの例:**
```json
{
  "id": "9a1b...",
  "title": "資料を送る",
  "schedule": { "type": "ONE_TIME", "date": "2026-06-26", "time": "09:00" },
  "enabled": true,
  "createdAt": "2026-06-25T20:00:00+09:00",
  "updatedAt": "2026-06-25T20:00:00+09:00"
}
```

- タイトルは最大 100 文字
- `enabled` フラグで有効／無効を切り替え（削除せずに一時停止できる）
- 削除すると物理削除される（ソフトデリートなし）

---

### 3. 自然言語インテーク（LLM 連携）

自然言語テキストから対話形式でリマインダー下書きを作成する機能。Google Gemini を使って構造化データへ変換する。

**フロー:**
1. ユーザーが自然言語テキストを送信（例: 「明日の9時に資料を送る」）
2. LLM がタイトルとスケジュールを抽出し、下書きを返す
3. 必要情報が不足している場合は `NEEDS_MORE_INFO` を返し、追加メッセージで補完
4. `READY` になったらクライアントが確認画面を表示
5. ユーザーが確定したら `POST /reminders` でリマインダーを作成

セッションはサーバーがインメモリで管理する。セッション失効後は `404` が返る。

`clientTime` を送ることで「30分後」などの相対表現をクライアントの現在時刻を基準に解釈できる。

**レスポンス例（READY）:**
```json
{
  "sessionId": "c4e2...",
  "status": "READY",
  "assistantMessage": "「明日の9時」を 2026-06-26 09:00 と解釈しました。この内容で登録しますか？",
  "draft": {
    "title": "資料を送る",
    "schedule": { "type": "ONE_TIME", "date": "2026-06-26", "time": "09:00" }
  },
  "missingElements": []
}
```

**レスポンス例（NEEDS_MORE_INFO）:**
```json
{
  "sessionId": "c4e2...",
  "status": "NEEDS_MORE_INFO",
  "assistantMessage": "何時に通知しますか？",
  "draft": { "title": "資料を送る", "schedule": null },
  "missingElements": ["SCHEDULE"]
}
```

LLM が解析不能なテキストを受け取った場合は `422` を返す。インテークが利用できない場合は `POST /reminders` で直接構造化データを送信するフォールバックを利用できる。

---

### 4. 学習項目管理（フラッシュカード）

単語・用語の学習項目を管理する機能。通知スケジューリングとランダム選出はモバイル端末が行う。

| 種別 | 用途 |
|---|---|
| `QA` | 質問・回答型（例: 「HTTP 404 の意味は？」→「リソースが見つからない」） |
| `TERM` | 用語・詳細型（例: 「冪等性」→「同じ操作を複数回行っても結果が変わらない性質」） |

いずれも `prompt`（表面）と `answer`（裏面）の2要素で構成される。

**学習項目リソースの例:**
```json
{
  "id": "7d3a...",
  "kind": "QA",
  "prompt": "HTTP 404 の意味は？",
  "answer": "リソースが見つからない",
  "enabled": true,
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### 5. 学習通知設定

1日に何回学習通知を受け取るかの設定。ユーザー1人につき1件の設定が存在する（登録時に自動作成）。

| 設定値 | 1日あたりの通知回数 |
|---|---|
| `ONCE` | 1回 |
| `THREE_TIMES` | 3回 |
| `FIVE_TIMES` | 5回 |

アクティブ時間帯は 9:00〜21:00 JST 固定（変更不可）。

---

### 6. 一括同期

モバイル端末のオフライン対応のため、全データを1回のリクエストで取得できる。

```json
{
  "reminders": [ ... ],
  "studyItems": [ ... ],
  "studyNotificationSetting": { ... },
  "serverTime": "2026-06-25T20:00:00+09:00"
}
```

---

## API エンドポイント

ベース URL: `http://localhost:8080/api/v1`（ローカル）

認証が必要なエンドポイントは `Authorization: Bearer <accessToken>` ヘッダを付与すること。

### 共通仕様

**エラーレスポンス（RFC 9457 Problem Details）:**
```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "タイトルは100文字以内にしてください",
  "errors": [
    { "field": "title", "message": "タイトルは100文字以内にしてください" }
  ]
}
```

**HTTP ステータスコード:**

| コード | 用途 |
|---|---|
| 200 | 取得・更新の成功 |
| 201 | 生成の成功（`Location` ヘッダ付与） |
| 204 | 削除の成功（本文なし） |
| 400 | バリデーションエラー |
| 401 | 未認証（トークン不正・欠落） |
| 404 | リソース未存在または認可違反 |
| 409 | 競合（メールアドレス重複など） |
| 422 | 自然言語の解析不能（インテーク時） |
| 500 | サーバ内部エラー |

---

### 認証 API（機能1: ユーザー認証）

#### POST /auth/register — ユーザー登録
認証不要。

リクエスト:
```json
{ "email_address": "alice@sample.com", "password": "P@ssw0rd!" }
```
レスポンス `201`:
```json
{ "id": "5f2c...", "email_address": "alice@sample.com" }
```
エラー: `409`（メールアドレス重複）、`400`（形式違反）

---

#### POST /auth/login — ログイン（JWT 発行）
認証不要。

リクエスト:
```json
{ "email_address": "alice@sample.com", "password": "P@ssw0rd!" }
```
レスポンス `200`:
```json
{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 1209600 }
```
- `expiresIn` は秒単位（1209600 = 14日）
- エラー: `401`（認証情報不一致）

---

#### GET /auth/me — 現在のユーザー取得
認証必須。

レスポンス `200`:
```json
{ "id": "5f2c...", "email_address": "alice@sample.com" }
```

---

### リマインダー API（機能2・3: リマインダー管理・自然言語インテーク）

#### POST /reminders/intake — 自然言語インテーク開始
認証必須。自然言語テキストを受け取り、対話セッションを開始する。

リクエスト:
```json
{ "text": "明日の9時に資料を送る", "clientTime": "2026-06-25T20:00:00+09:00" }
```
- `clientTime`: 任意。相対表現（「30分後」等）の基準時刻。省略時はサーバ受信時刻を使用。

レスポンス `200`:
```json
{
  "sessionId": "c4e2...",
  "status": "READY",
  "assistantMessage": "「明日の9時」を 2026-06-26 09:00 と解釈しました。この内容で登録しますか？",
  "draft": {
    "title": "資料を送る",
    "schedule": { "type": "ONE_TIME", "date": "2026-06-26", "time": "09:00" }
  },
  "missingElements": []
}
```
- `status`: `READY`（必須要素が揃った）/ `NEEDS_MORE_INFO`（不足あり）
- `missingElements`: `["TITLE"]`、`["SCHEDULE"]` など
- エラー: `422`（解析不能）

---

#### POST /reminders/intake/{sessionId}/messages — インテーク継続
認証必須。不足要素を補完するための追加メッセージを送る。

リクエスト:
```json
{ "text": "タイトルは会議準備にして" }
```
レスポンス `200`: インテーク開始と同形（更新後の `draft` と次の質問、または `READY`）

- セッション失効後は `404`

---

#### POST /reminders — リマインダー作成
認証必須。確定した構造化データでリマインダーを登録する。インテーク後の確定時、または手動フォールバック時に使用。

リクエスト:
```json
{
  "title": "会議準備",
  "schedule": { "type": "WEEKLY", "dayOfWeek": "MONDAY", "time": "09:00" }
}
```
レスポンス `201`（`Location: /api/v1/reminders/{id}`）: リマインダーリソース

---

#### GET /reminders — リマインダー一覧
認証必須。

クエリパラメータ（任意）:
- `enabled`: `true` / `false` でフィルタ
- `updatedSince`: ISO-8601 日時で差分取得

レスポンス `200`:
```json
{ "items": [ { "id": "9a1b...", "title": "...", "schedule": { ... }, "enabled": true, "createdAt": "...", "updatedAt": "..." } ] }
```

---

#### GET /reminders/{id} — リマインダー取得
認証必須。

レスポンス `200`: リマインダーリソース / `404`

---

#### PUT /reminders/{id} — リマインダー編集
認証必須。タイトルとスケジュールを全置換する。

リクエスト:
```json
{ "title": "会議準備（更新）", "schedule": { "type": "DAILY", "time": "08:30" } }
```
レスポンス `200`: 更新後リマインダーリソース

---

#### PATCH /reminders/{id} — 有効／無効切替
認証必須。有効／無効の状態のみ部分更新する。

リクエスト:
```json
{ "enabled": false }
```
レスポンス `200`: 更新後リマインダーリソース

---

#### DELETE /reminders/{id} — リマインダー削除
認証必須。

レスポンス `204`（本文なし）

---

### 学習 API（機能4・5: 学習項目管理・学習通知設定）

#### POST /study-items — 学習項目作成
認証必須。

リクエスト:
```json
{ "kind": "TERM", "prompt": "冪等性", "answer": "同じ操作を複数回行っても結果が変わらない性質" }
```
レスポンス `201`（`Location` 付与）: 学習項目リソース

---

#### GET /study-items — 学習項目一覧
認証必須。

クエリパラメータ（任意）:
- `enabled`: `true` / `false` でフィルタ
- `updatedSince`: ISO-8601 日時で差分取得

レスポンス `200`: `{ "items": [ ... ] }`

---

#### GET /study-items/{id} — 学習項目取得
認証必須。

レスポンス `200`: 学習項目リソース / `404`

---

#### PUT /study-items/{id} — 学習項目編集
認証必須。

リクエスト:
```json
{ "kind": "TERM", "prompt": "冪等性", "answer": "（更新後の説明）" }
```
レスポンス `200`: 更新後学習項目リソース

---

#### PATCH /study-items/{id} — 学習項目 有効／無効切替
認証必須。

リクエスト:
```json
{ "enabled": false }
```
レスポンス `200`: 更新後学習項目リソース

---

#### DELETE /study-items/{id} — 学習項目削除
認証必須。

レスポンス `204`（本文なし）

---

#### GET /study/notification-setting — 学習通知設定取得
認証必須。ユーザーに紐づく1件の設定を返す。

レスポンス `200`:
```json
{
  "frequency": "THREE_TIMES",
  "activeHours": { "start": "09:00", "end": "21:00" },
  "enabled": true
}
```
- `activeHours` は 9:00〜21:00 JST 固定（変更不可）

---

#### PUT /study/notification-setting — 学習通知設定更新
認証必須。

リクエスト:
```json
{ "frequency": "FIVE_TIMES", "enabled": true }
```
レスポンス `200`: 更新後設定

---

### 同期 API（機能6: 一括同期）

#### GET /sync — 一括スナップショット取得
認証必須。リマインダー・学習項目・通知設定をまとめて取得する。モバイル端末の起動時同期に使用する。

レスポンス `200`:
```json
{
  "reminders": [ ... ],
  "studyItems": [ ... ],
  "studyNotificationSetting": { ... },
  "serverTime": "2026-06-25T20:00:00+09:00"
}
```

---

### エンドポイント早見表

| メソッド | パス | 機能 | 認証 |
|---|---|---|---|
| POST | `/api/v1/auth/register` | ユーザー登録 | 不要 |
| POST | `/api/v1/auth/login` | ログイン（JWT 発行） | 不要 |
| GET | `/api/v1/auth/me` | 現在のユーザー取得 | 要 |
| POST | `/api/v1/reminders/intake` | 自然言語インテーク開始 | 要 |
| POST | `/api/v1/reminders/intake/{sessionId}/messages` | インテーク継続（不足補完） | 要 |
| POST | `/api/v1/reminders` | リマインダー作成 | 要 |
| GET | `/api/v1/reminders` | リマインダー一覧 | 要 |
| GET | `/api/v1/reminders/{id}` | リマインダー取得 | 要 |
| PUT | `/api/v1/reminders/{id}` | リマインダー編集 | 要 |
| PATCH | `/api/v1/reminders/{id}` | 有効／無効切替 | 要 |
| DELETE | `/api/v1/reminders/{id}` | リマインダー削除 | 要 |
| POST | `/api/v1/study-items` | 学習項目作成 | 要 |
| GET | `/api/v1/study-items` | 学習項目一覧 | 要 |
| GET | `/api/v1/study-items/{id}` | 学習項目取得 | 要 |
| PUT | `/api/v1/study-items/{id}` | 学習項目編集 | 要 |
| PATCH | `/api/v1/study-items/{id}` | 学習項目 有効／無効切替 | 要 |
| DELETE | `/api/v1/study-items/{id}` | 学習項目削除 | 要 |
| GET | `/api/v1/study/notification-setting` | 学習通知設定取得 | 要 |
| PUT | `/api/v1/study/notification-setting` | 学習通知設定更新 | 要 |
| GET | `/api/v1/sync` | 一括スナップショット取得 | 要 |

## NeonDBへのマイグレーション
```aiignore
cat > run-local.sh << 'EOF'
#!/bin/bash
export SPRING_PROFILES_ACTIVE=local
export SPRING_DATASOURCE_URL='jdbc:postgresql://ep-curly-wave-az1lgh3y-pooler.c-3.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require'
export SPRING_DATASOURCE_USERNAME="neondb_owner"
export SPRING_DATASOURCE_PASSWORD=""
export JWT_SECRET=""
./gradlew bootRun
EOF
chmod +x run-local.sh
./run-local.sh
```