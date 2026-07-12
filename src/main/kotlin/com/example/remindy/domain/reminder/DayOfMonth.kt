package com.example.remindy.domain.reminder

/**
 * 毎月リマインダーの「日にち」(1〜31)。java.time に該当型が無いので値オブジェクト化する。
 *
 * 補足(責務境界): 「31日が無い月にどう発火するか」は"次回発火時刻の計算"であり、
 * これは端末側の責務。よってドメインは 1〜31 の範囲検証だけに徹し、暦の解決はしない。
 */
@JvmInline
value class DayOfMonth private constructor(val value: Int) {
    companion object {
        fun of(value: Int): DayOfMonth {
            require(value in 1..31) { "日にちは1〜31で指定してください（指定値: $value）" }
            return DayOfMonth(value)
        }
    }
}