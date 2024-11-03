package pl.edu.agh.gem.internal.client

interface EmailSenderClient {
    fun notifyAboutReport(
        reportId: String,
        title: String,
        creatorId: String,
        attachmentId: String,
        groupId: String,
    )
}

class EmailSenderClientException(override val message: String?) : RuntimeException()

class RetryableEmailSenderClientException(override val message: String?) : RuntimeException()
