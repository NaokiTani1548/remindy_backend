# リマインダーアプリ API 仕様書（ドラフト v0.1）

> 対象: バックエンド REST API。要件定義書 v0.2 に基づく。
> 〔要確認〕は設計上の論点。第8章に一覧化。確定後に版を更新する。

---

## 1. 概要・前提

| 項目 | 内容 |
|---|---|
| プロトコル | HTTPS |
| 形式 | リクエスト/レスポンスともに JSON（`application/json`） |
| ベースパス | `/api/v1`（URL パス方式でバージョニング） |
| 認証 | JWT（アクセストークンのみ。リフレッシュなし）。`Authorization: Bearer <token>` |
| タイムゾーン | すべての壁時計時刻は JST 固定 |
| 識別子 | UUID 文字列 |
| 文字コード | UTF-8 |

- 通知の発火・スケジュール計算・学習項目のランダム選出は **モバイル端末の責務**であり、本 API には含まれない。本 API はデータの CRUD・同期・自然言語の構造化のみを担う。
- 本 API は Android／Web 両クライアントで共有する（Web は登録・編集・削除のみ利用）。

---

## 2. 共通仕様

### 2.1 認証
- `POST /auth/register`、`POST /auth/login` を除く全エンドポイントで `Authorization: Bearer <accessToken>` が必須。
- トークン不正・欠落時は `401`。他ユーザーの資源にアクセスした場合の挙動は〔要確認〕（第8章 Q5、推奨は存在を秘匿するため `404`）。

### 2.2 日時・スケジュールの表現
- 日付: `YYYY-MM-DD`（例 `2026-07-01`）
- 時刻（時・分）: `HH:mm`（例 `09:00`、JST）
- 曜日: `MONDAY` 〜 `SUNDAY`（ISO 準拠）
- 日にち: `DD` (例 25)
- 監査用日時（`createdAt` 等）: オフセット付き ISO-8601（例 `2026-06-25T12:34:56+09:00`）
- スケジュールは種別で判別する判別共用体（discriminated union）として表現する:

```json
{ "type": "ONE_TIME", "date": "2026-07-01", "time": "09:00" }
```
```json
{ "type": "DAILY", "time": "07:30" }
```
```json
{ "type": "WEEKLY", "dayOfWeek": "MONDAY", "time": "21:00" }
```
```json
{ "type": "MONTHLY", "dayOfMonth": 25, "time": "21:00" }
```

| type       | 必須フィールド              |
|------------|----------------------|
| `ONE_TIME` | `date`, `time`       |
| `DAILY`    | `time`               |
| `WEEKLY`   | `dayOfWeek`, `time`  |
| `MONTHLY`  | `dayOfMonth`, `time` |

### 2.3 エラー表現
RFC 9457 Problem Details（`application/problem+json`）を採用する〔要確認 Q4〕。

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

### 2.4 HTTP ステータス方針
| コード | 用途                       |
|---|--------------------------|
| 200 | 取得・更新の成功                 |
| 201 | 生成の成功（`Location` ヘッダを付与） |
| 204 | 削除の成功（本文なし）              |
| 400 | バリデーションエラー               |
| 401 | 未認証（トークン不正・欠落）           |
| 403 / 404 | 認可違反（〔要確認 Q5〕推奨は 404）    |
| 409 | 競合（メールアドレス 重複など）         |
| 422 | 自然言語の解析不能（インテーク時）        |
| 500 | サーバ内部エラー                 |

### 2.5 ページング
当面は実装しない（個人・小規模のため一覧は全件返却）。将来必要になればカーソル方式を追加〔要確認 Q3 と関連〕。

---

## 3. 認証 API

### 3.1 ユーザー登録
`POST /api/v1/auth/register`（認証不要）

リクエスト:
```json
{ "email_address": "alice@sample.com", "password": "P@ssw0rd!" }
```
レスポンス `201`:
```json
{ "id": "5f2c…", "email_address": "alice@sample.com" }
```
エラー: `409`（メールアドレス 重複）, `400`（形式違反）

### 3.2 ログイン
`POST /api/v1/auth/login`（認証不要）

リクエスト:
```json
{ "email_address": "alice@sample.com", "password": "P@ssw0rd!" }
```
レスポンス `200`:
```json
{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 1209600 }
```
- `expiresIn` は秒（例は 14 日）。具体値は技術選定で確定。
- エラー: `401`（認証情報不一致）

### 3.3 現在のユーザー取得
`GET /api/v1/auth/me`

レスポンス `200`:
```json
{ "id": "5f2c…", "email_address": "alice@sample.com" }
```

---

## 4. リマインダー API

リマインダー資源の表現:
```json
{
  "id": "9a1b…",
  "title": "資料を送る",
  "schedule": { "type": "ONE_TIME", "date": "2026-06-26", "time": "09:00" },
  "enabled": true,
  "createdAt": "2026-06-25T20:00:00+09:00",
  "updatedAt": "2026-06-25T20:00:00+09:00"
}
```

### 4.1 自然言語インテーク（対話収集）〔要確認 Q1：方式 A を推奨〕
自然言語からリマインダーの下書き（draft）を作る対話フェーズ。**作成そのものは行わない**（作成は 4.2）。サーバが収集セッションの状態を保持し、LLM は1ターンごとのステートレス解析に徹する（要件 FR-N6）。

#### 4.1.1 インテーク開始
`POST /api/v1/reminders/intake`

リクエスト:
```json
{ "text": "明日の9時に資料を送る", "clientTime": "2026-06-25T20:00:00+09:00" }
```
- `clientTime`（任意）: 相対表現（「30分後」等）の基準時刻。省略時はサーバ受信時刻（JST）を用いる〔要確認 Q2〕。

レスポンス `200`:
```json
{
  "sessionId": "c4e2…",
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
- `missingElements`: `["TITLE"]` `["SCHEDULE"]` などの不足要素
- 解析不能時: `422`

#### 4.1.2 インテーク継続（不足要素の補完）
`POST /api/v1/reminders/intake/{sessionId}/messages`

リクエスト:
```json
{ "text": "タイトルは会議準備にして" }
```
レスポンス `200`: 4.1.1 と同形（更新後の `draft` と次の質問、または `READY`）

- セッションは一定時間で失効する（具体値は実装時）。失効時は `404`。
- 確認画面での編集はクライアント側で `draft` を直接書き換えてよい（サーバ往復不要）。確定時に 4.2 を呼ぶ。

### 4.2 リマインダー作成（確認後／手動フォールバック）
`POST /api/v1/reminders`

確認画面で確定した内容、または解析失敗時の手動入力（FR-N3）を構造化データとして受け取り、登録する。

リクエスト:
```json
{
  "title": "会議準備",
  "schedule": { "type": "WEEKLY", "dayOfWeek": "MONDAY", "time": "09:00" }
}
```
レスポンス `201`（`Location: /api/v1/reminders/{id}`）: リマインダー資源
エラー: `400`（バリデーション。未来日時でない等の検証もここで実施）

### 4.3 リマインダー一覧
`GET /api/v1/reminders`

クエリ（任意）: `enabled`（true/false）, `updatedSince`（ISO-8601。差分同期用〔要確認 Q3〕）

レスポンス `200`:
```json
{ "items": [ { "id": "9a1b…", "title": "…", "schedule": { … }, "enabled": true, "createdAt": "…", "updatedAt": "…" } ] }
```

### 4.4 リマインダー取得
`GET /api/v1/reminders/{id}` → `200` リマインダー資源 / `404`

### 4.5 リマインダー編集（構造化フォーム）
`PUT /api/v1/reminders/{id}`

リクエスト:
```json
{ "title": "会議準備（更新）", "schedule": { "type": "DAILY", "time": "08:30" } }
```
レスポンス `200`: 更新後資源

### 4.6 有効／無効の切替
`PATCH /api/v1/reminders/{id}`

リクエスト:
```json
{ "enabled": false }
```
レスポンス `200`: 更新後資源
- 設計意図: `PUT` は内容（タイトル・スケジュール）の置換、`PATCH` は状態（有効/無効）の部分更新、と役割を分ける。

### 4.7 リマインダー削除
`DELETE /api/v1/reminders/{id}` → `204`

---

## 5. 単語学習 API

> ランダム選出と通知は端末側の責務のため、「次に通知する項目を返す」ような API は提供しない。本 API は学習項目と通知設定の保持のみを担う。

学習項目資源:
```json
{
  "id": "7d3a…",
  "kind": "QA",
  "prompt": "HTTP 404 の意味は？",
  "answer": "リソースが見つからない",
  "enabled": true,
  "createdAt": "…",
  "updatedAt": "…"
}
```
`kind`: `QA`（質問・回答型）/ `TERM`（用語・詳細型）。いずれも `prompt`（表）・`answer`（裏）の2要素。

### 5.1 学習項目 作成
`POST /api/v1/study-items`
```json
{ "kind": "TERM", "prompt": "冪等性", "answer": "同じ操作を複数回行っても結果が変わらない性質" }
```
→ `201`（`Location` 付与）

### 5.2 学習項目 一覧
`GET /api/v1/study-items`（クエリ `enabled`, `updatedSince` 任意）→ `200` `{ "items": [ … ] }`

### 5.3 学習項目 取得
`GET /api/v1/study-items/{id}` → `200` / `404`

### 5.4 学習項目 編集
`PUT /api/v1/study-items/{id}`
```json
{ "kind": "TERM", "prompt": "冪等性", "answer": "（更新後の説明）" }
```
→ `200`

### 5.5 学習項目 有効／無効の切替
`PATCH /api/v1/study-items/{id}`
```json
{ "enabled": false }
```
→ `200`

### 5.6 学習項目 削除
`DELETE /api/v1/study-items/{id}` → `204`

### 5.7 学習通知設定 取得
`GET /api/v1/study/notification-setting`（ユーザーに1件）

レスポンス `200`:
```json
{
  "frequency": "THREE_TIMES",
  "activeHours": { "start": "09:00", "end": "21:00" },
  "enabled": true
}
```
- `frequency`: `ONCE` / `THREE_TIMES` / `FIVE_TIMES`（1日あたり）
- `activeHours`: 9:00–21:00 固定（参照用に返すが変更不可）

### 5.8 学習通知設定 更新
`PUT /api/v1/study/notification-setting`
```json
{ "frequency": "FIVE_TIMES", "enabled": true }
```
レスポンス `200`: 更新後設定
- `activeHours` は固定のため、送られても無視（または `400`）〔実装時に確定〕。

---

## 6. 同期 API

モバイルはオフライン通知のため、データをローカルに保持して同期する。

### 6.1 一括スナップショット取得（推奨・初期方式）
`GET /api/v1/sync`

レスポンス `200`:
```json
{
  "reminders": [ … ],
  "studyItems": [ … ],
  "studyNotificationSetting": { … },
  "serverTime": "2026-06-25T20:00:00+09:00"
}
```
- 1回の呼び出しで端末が必要とする全データを返す利便用エンドポイント。
- 差分同期（`updatedSince` 併用）は将来の最適化として検討〔要確認 Q3〕。各一覧 API（4.3/5.2）でも代替可能。

---

## 7. エンドポイント一覧

| メソッド | パス | 概要 | 認証 |
|---|---|---|---|
| POST | `/api/v1/auth/register` | ユーザー登録 | 不要 |
| POST | `/api/v1/auth/login` | ログイン（JWT 発行） | 不要 |
| GET | `/api/v1/auth/me` | 現在のユーザー〔任意〕 | 要 |
| POST | `/api/v1/reminders/intake` | 自然言語インテーク開始 | 要 |
| POST | `/api/v1/reminders/intake/{sessionId}/messages` | インテーク継続 | 要 |
| POST | `/api/v1/reminders` | リマインダー作成 | 要 |
| GET | `/api/v1/reminders` | 一覧 | 要 |
| GET | `/api/v1/reminders/{id}` | 取得 | 要 |
| PUT | `/api/v1/reminders/{id}` | 編集 | 要 |
| PATCH | `/api/v1/reminders/{id}` | 有効/無効切替 | 要 |
| DELETE | `/api/v1/reminders/{id}` | 削除 | 要 |
| POST | `/api/v1/study-items` | 学習項目作成 | 要 |
| GET | `/api/v1/study-items` | 一覧 | 要 |
| GET | `/api/v1/study-items/{id}` | 取得 | 要 |
| PUT | `/api/v1/study-items/{id}` | 編集 | 要 |
| PATCH | `/api/v1/study-items/{id}` | 有効/無効切替 | 要 |
| DELETE | `/api/v1/study-items/{id}` | 削除 | 要 |
| GET | `/api/v1/study/notification-setting` | 学習通知設定取得 | 要 |
| PUT | `/api/v1/study/notification-setting` | 学習通知設定更新 | 要 |
| GET | `/api/v1/sync` | 一括スナップショット | 要 |

---

## 8. 要確認事項（API 設計の論点）

| # | 論点 | 選択肢 | 推奨 |
|---|---|---|---|
| Q1 | 対話収集の方式 | A: サーバがセッション状態を保持（`sessionId`） / B: ステートレス（クライアントが文脈を毎回送る） | **A**（要件 FR-N6 と整合。サーバが収集を管理、LLM はステートレス） |
| Q2 | 相対時刻の基準 | サーバ受信時刻 / クライアント送信時刻(`clientTime`) | **任意の `clientTime` を受け、無ければサーバ時刻** |
| Q3 | 同期方式 | 全件スナップショット / 差分（`updatedSince`） | **初期は全件（`/sync`）**、差分は将来 |
| Q4 | エラー形式 | RFC 9457 Problem Details / 独自 JSON | **Problem Details**（Spring 標準で学習価値も高い） |
| Q5 | 非所有リソース | `403` / `404` | **404**（存在を秘匿） |
| Q6 | `/auth/me` | 提供する / しない | **提供する**（クライアント実装が楽） |
| Q7 | 直接構造化作成 `POST /reminders` | 公開する / インテーク経由のみ | **公開する**（手動フォールバック FR-N3 に必要） |

---

## 9. スコープ外（本 API では提供しない）

- 通知の送信・スケジューリング（端末側）
- 学習項目のランダム選出（端末側）
- Web 向けプッシュ通知
- ページング（当面）

---

## 改訂履歴
| 版 | 日付 | 内容 |
|---|---|---|
| v0.1 | (ドラフト) | 初版ドラフト作成 |