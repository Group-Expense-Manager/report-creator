package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import org.bson.types.Binary
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubPostReportUrl
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.util.createAttachment

class AttachmentStoreClientIT(
    private val attachmentStoreClient: AttachmentStoreClient,
) : BaseIntegrationSpec({
    should("upload attachment") {
        // given
        val attachmentResponse = createAttachment()
        stubPostReportUrl(body = attachmentResponse, userId = USER_ID, groupId = GROUP_ID)

        // when
        val result = attachmentStoreClient.uploadAttachment(GROUP_ID, USER_ID, Binary(ByteArray(0)))

        // then
        result.attachmentId.shouldNotBeNull()
    }

    should("handle 4xx error response") {
        // given
        stubPostReportUrl(groupId = GROUP_ID, userId = USER_ID, statusCode = BAD_REQUEST)

        // when & then
        // workaround for IOException
        shouldThrow<Exception> {
            attachmentStoreClient.uploadAttachment(GROUP_ID, USER_ID, Binary(ByteArray(0)))
        }
    }

    should("handle 5xx error response") {
        // given
        stubPostReportUrl(groupId = GROUP_ID, userId = USER_ID, statusCode = INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableAttachmentStoreClientException> {
            attachmentStoreClient.uploadAttachment(GROUP_ID, USER_ID, Binary(ByteArray(0)))
        }
    }
},)
