package com.spamguard.core.domain.phone

import javax.inject.Inject

/**
 * Đưa số điện thoại về một dạng duy nhất để so sánh:
 * bỏ khoảng trắng, dấu gạch, ngoặc và đổi mã quốc gia Việt Nam (+84, 0084) thành số 0.
 *
 * Ví dụ: `+84 900-123-456` → `0900123456`.
 */
class PhoneNumberNormalizer @Inject constructor() {

    fun normalize(rawNumber: String): String {
        val compact = rawNumber.filter { it.isDigit() || it == PLUS_SIGN }
        val national = when {
            compact.startsWith(VIETNAM_PLUS_PREFIX) ->
                NATIONAL_TRUNK_PREFIX + compact.removePrefix(VIETNAM_PLUS_PREFIX)

            compact.startsWith(VIETNAM_IDD_PREFIX) ->
                NATIONAL_TRUNK_PREFIX + compact.removePrefix(VIETNAM_IDD_PREFIX)

            else -> compact
        }
        return national.filter(Char::isDigit)
    }

    private companion object {
        const val PLUS_SIGN = '+'
        const val VIETNAM_PLUS_PREFIX = "+84"
        const val VIETNAM_IDD_PREFIX = "0084"
        const val NATIONAL_TRUNK_PREFIX = "0"
    }
}
