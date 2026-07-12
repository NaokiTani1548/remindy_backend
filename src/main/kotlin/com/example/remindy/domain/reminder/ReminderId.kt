package com.example.remindy.domain.reminder

import java.util.UUID

/** リマインダーの識別子。UserId と同じ理由で value class。 */
@JvmInline
value class ReminderId(val value: UUID)