package com.spamguard.core.domain.phone

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberNormalizerTest {

    private val normalizer = PhoneNumberNormalizer()

    @Test
    fun `removes spaces, dashes and parentheses`() {
        assertEquals("0900123456", normalizer.normalize("(0900) 123-456"))
    }

    @Test
    fun `converts +84 country code to leading zero`() {
        assertEquals("0900123456", normalizer.normalize("+84 900 123 456"))
    }

    @Test
    fun `converts 0084 international prefix to leading zero`() {
        assertEquals("0900123456", normalizer.normalize("0084900123456"))
    }

    @Test
    fun `keeps foreign numbers as digits only`() {
        assertEquals("16505550100", normalizer.normalize("+1 (650) 555-0100"))
    }

    @Test
    fun `returns empty string for hidden numbers`() {
        assertEquals("", normalizer.normalize(""))
    }
}
