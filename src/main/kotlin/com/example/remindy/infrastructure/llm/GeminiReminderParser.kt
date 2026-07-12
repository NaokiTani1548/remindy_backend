package com.example.remindy.infrastructure.llm

import com.example.remindy.application.reminder.intake.ReminderDraft
import com.example.remindy.application.reminder.intake.ReminderParseException
import com.example.remindy.application.reminder.intake.ReminderParser
import com.example.remindy.domain.reminder.DayOfMonth
import com.example.remindy.domain.reminder.Schedule
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Gemini(generateContent)へプロンプトを投げ、JSONで構造化ドラフトを受け取るアダプタ。
 * LLMには「相対表現を referenceTime(JST) を基準に絶対日時へ解決」させ、結果をJSONで返させる。
 * 機微情報は送らない方針のため、本文テキストのみ送信する。
 */
@Component
class GeminiReminderParser(
    private val properties: LlmProperties,
    private val objectMapper: ObjectMapper,
) : ReminderParser {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .build()

    override fun parse(text: String, referenceTime: OffsetDateTime): ReminderDraft {
        if (properties.apiKey.isBlank()) {
            throw ReminderParseException("LLM APIキーが設定されていません")
        }
        val prompt = buildPrompt(text, referenceTime)
        val body = mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(mapOf("text" to prompt)))
            ),
            "generationConfig" to mapOf("responseMimeType" to "application/json"),
        )

        val response: JsonNode = try {
            restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", properties.model, properties.apiKey)
                .body(body)
                .retrieve()
                .body(JsonNode::class.java)
                ?: throw ReminderParseException("LLM応答が空でした")
        } catch (e: Exception) {
            throw ReminderParseException("LLM呼び出しに失敗しました: ${e.message}")
        }

        val jsonText = response
            .path("candidates").path(0)
            .path("content").path("parts").path(0)
            .path("text").asText(null)
            ?: throw ReminderParseException("LLM応答の解析に失敗しました")

        return toDraft(parseJson(jsonText))
    }

    private fun buildPrompt(text: String, referenceTime: OffsetDateTime): String = """
        あなたはリマインダー登録アシスタントです。ユーザーの入力からタイトルと通知タイミングを抽出し、
        JSONのみを出力してください（前後の説明やマークダウンは不要）。

        基準時刻(JST): ${referenceTime}
        「明日」「30分後」などの相対表現はこの基準時刻を用いて絶対的な日付・時刻へ解決してください。

        出力スキーマ:
        {
          "title": string|null,
          "schedule": null | 
            { "type":"ONE_TIME", "date":"YYYY-MM-DD", "time":"HH:mm" } |
            { "type":"DAILY", "time":"HH:mm" } |
            { "type":"WEEKLY", "dayOfWeek":"MONDAY|...|SUNDAY", "time":"HH:mm" } |
            { "type":"MONTHLY", "dayOfMonth":1-31, "time":"HH:mm" }
        }
        抽出できない要素はnullにしてください。

        ユーザー入力: ${text}
    """.trimIndent()

    private fun parseJson(raw: String): JsonNode =
        try {
            objectMapper.readTree(raw.trim())
        } catch (e: Exception) {
            throw ReminderParseException("LLMが不正なJSONを返しました")
        }

    private fun toDraft(node: JsonNode): ReminderDraft {
        val title = node.path("title").asText(null)?.takeIf { it.isNotBlank() }
        val scheduleNode = node.get("schedule")
        val schedule = if (scheduleNode == null || scheduleNode.isNull) null else toSchedule(scheduleNode)
        return ReminderDraft(title = title, schedule = schedule)
    }

    private fun toSchedule(node: JsonNode): Schedule? {
        val time = node.path("time").asText(null)?.let(LocalTime::parse) ?: return null
        return when (node.path("type").asText("")) {
            "ONE_TIME" -> node.path("date").asText(null)?.let { Schedule.OneTime(LocalDate.parse(it), time) }
            "DAILY" -> Schedule.Daily(time)
            "WEEKLY" -> node.path("dayOfWeek").asText(null)?.let { Schedule.Weekly(DayOfWeek.valueOf(it), time) }
            "MONTHLY" -> node.path("dayOfMonth").takeIf { it.isInt || it.isTextual }
                ?.let { Schedule.Monthly(DayOfMonth.of(it.asInt()), time) }
            else -> null
        }
    }
}
