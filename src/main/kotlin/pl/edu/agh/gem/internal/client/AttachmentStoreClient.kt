package pl.edu.agh.gem.internal.client

import org.bson.types.Binary
import pl.edu.agh.gem.internal.model.finance.Attachment

interface AttachmentStoreClient {
    fun uploadAttachment(groupId: String, userId: String, file: Binary): Attachment
}

class AttachmentStoreClientException(override val message: String?) : RuntimeException()

class RetryableAttachmentStoreClientException(override val message: String?) : RuntimeException()
