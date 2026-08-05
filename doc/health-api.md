# Health API — サーバー接続管理

## 背景

Remindy バックエンドは Render の無料プランで動作しているため、一定時間リクエストがないとサーバーがスリープ状態に入ります。次のリクエストが来るとサーバーが起動しますが、完全に応答できるようになるまで **最大 1 分程度** かかります。

この遅延をユーザーに自然に体験させるため、以下の 2 つのエンドポイントを用意しています。

---

## エンドポイント

ベース URL: `https://<your-render-domain>`
※ `/api/v1` プレフィックスは **付きません**。認証も不要です。

---

### GET /health — ヘルスチェック（即時）

サーバーが今すぐ応答できるかを確認します。応答に時間はかかりません（約 1 秒以内）。

**用途：** アプリ起動時や画面上の「接続状態ランプ」の定期ポーリングに使用します。

#### レスポンス

| ステータス | 意味 |
|---|---|
| `200 OK` | サーバーは正常に稼働中 |
| `503 Service Unavailable` | サーバーはスリープ中または起動中 |

レスポンスボディは空です。

#### 使用例（Kotlin / OkHttp）

```kotlin
fun checkHealth(): Boolean {
    val request = Request.Builder()
        .url("$baseUrl/health")
        .get()
        .build()
    return client.newCall(request).execute().use { it.code == 200 }
}
```

---

### GET /health/connect — ウェイクアップ接続（ロングポーリング）

サーバーが応答できるようになるまでサーバー側で待機し、準備完了したら返します。

**用途：** ユーザーが「接続」ボタンを押したとき、または `/health` が 503 を返したときに呼び出します。

- サーバーがすでに起動済みの場合 → **即座に 200 を返す**
- サーバーがスリープ中の場合 → **サーバーが復帰するまで接続を保持し（最大 5 分）、復帰したら 200 を返す**

#### レスポンス

| ステータス | 意味 |
|---|---|
| `200 OK` | サーバーが応答可能になった |
| `503 Service Unavailable` | 5 分以内に復帰しなかった（タイムアウト） |

レスポンスボディは空です。

#### 注意事項

- HTTP タイムアウトを **310 秒以上** に設定してください（サーバー側の最大待機は 300 秒）
- このリクエスト中はサーバー側スレッドをブロックしません（ロングポーリング実装）
- 503 が返った場合はリトライするか、ユーザーにエラーを表示してください

#### 使用例（Kotlin / OkHttp）

```kotlin
suspend fun waitUntilReady(): Boolean {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(310, TimeUnit.SECONDS)  // サーバー側最大 300 秒 + 余裕
        .build()

    val request = Request.Builder()
        .url("$baseUrl/health/connect")
        .get()
        .build()

    return withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(request).execute().use { it.code == 200 }
        }.getOrDefault(false)
    }
}
```

---

## 推奨フロー

```
アプリ起動
    │
    ▼
GET /health
    │
    ├─ 200 → そのまま通常のAPIを呼び出す
    │
    └─ 503 または タイムアウト
            │
            ▼
        「接続中...」UI を表示
            │
            ▼
        GET /health/connect（最大 5 分待機）
            │
            ├─ 200 → 接続完了。通常のAPIを呼び出す
            │
            └─ 503 → 「サーバーへの接続に失敗しました」を表示
```

### UI 上の接続ランプ

定期的に `/health` をポーリングして接続状態を表示します。

```kotlin
// 例: 30 秒ごとにポーリング
LaunchedEffect(Unit) {
    while (true) {
        val isHealthy = checkHealth()
        updateConnectionStatus(isHealthy)
        delay(30_000)
    }
}
```

---

## まとめ

| | `/health` | `/health/connect` |
|---|---|---|
| 目的 | 状態確認（ランプ表示） | ウェイクアップ待機（接続ボタン） |
| 応答タイミング | 即時（〜1 秒） | サーバー復帰まで待機（最大 5 分） |
| クライアント timeout 設定 | デフォルトで OK | **310 秒以上必須** |
| 認証 | 不要 | 不要 |
| ベース URL | `https://<domain>` | `https://<domain>` |
