package com.inkdrop.app.util

object UrlValidator {

    private val TWITTER_URL_PATTERNS = listOf(
        Regex("""https?://(www\.)?(twitter|x)\.com/\w+/status/\d+.*"""),
        Regex("""https?://(mobile\.)?(twitter|x)\.com/\w+/status/\d+.*"""),
        Regex("""https?://(www\.)?(twitter|x)\.com/i/web/status/\d+.*""")
    )

    fun isValidTwitterUrl(url: String): Boolean {
        return TWITTER_URL_PATTERNS.any { it.matches(url) }
    }

    fun extractTwitterUrl(text: String): String? {
        // Look for Twitter/X URLs in shared text
        val urlPattern = Regex("""https?://[^\s]+""")
        val urls = urlPattern.findAll(text)

        for (match in urls) {
            val url = match.value.trimEnd(')', ']', '>', '"', '\'')
            if (isValidTwitterUrl(url)) {
                return url
            }
        }

        // Also check the whole text if it's just a URL
        val trimmed = text.trim()
        if (isValidTwitterUrl(trimmed)) {
            return trimmed
        }

        return null
    }
}
