package com.melashkov.mcparking.ui.support

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedbackValidatorTest {
    @Test fun validatesTrimmedMessageBoundaries() {
        for (length in listOf(0, 4, 5, 1500, 1501)) {
            val result = FeedbackValidator.validate(" ${"a".repeat(length)} ", "")
            assertEquals(length, result.messageLength)
            assertEquals(length in 5..1500, result.valid)
            assertEquals(length > 1500, result.messageTooLong)
        }
    }

    @Test fun rejectsControlCharactersButAllowsMultilineText() {
        for (code in (0..31) + 127) {
            val result = FeedbackValidator.validate("hello${code.toChar()}world", "")
            assertEquals(code in listOf(9, 10, 13), result.messageValid, "Character code $code")
        }
        assertTrue(FeedbackValidator.validate("A useful\nidea\twith details\r\n", "").valid)
    }

    @Test fun validatesOptionalEmailAndLengthBoundary() {
        for (email in listOf("", "  ", "me@example.org", " me@example.org ", "a+b@example.co.uk")) {
            assertTrue(FeedbackValidator.validate("A useful idea", email).emailValid, email)
        }
        for (email in listOf("invalid", "me@example", "me@@example.org", "me @example.org", "me@example.org\nextra")) {
            assertFalse(FeedbackValidator.validate("A useful idea", email).emailValid, email)
        }
        val maximumEmail = "a".repeat(242) + "@example.org"
        assertEquals(254, maximumEmail.length)
        assertTrue(FeedbackValidator.validate("A useful idea", " $maximumEmail ").emailValid)
        assertFalse(FeedbackValidator.validate("A useful idea", "a$maximumEmail").emailValid)
    }
}
