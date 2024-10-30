package pl.edu.agh.gem.internal.client

interface EmailSenderClient {
    fun notifyAboutReport(reportId: String, title: String, userId: String, attachmentId: String)
}

class EmailSenderClientException(override val message: String?) : RuntimeException()

class RetryableEmailSenderClientException(override val message: String?) : RuntimeException()
