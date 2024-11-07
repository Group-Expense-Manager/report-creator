package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.attachment.AttachmentResponse
import pl.edu.agh.gem.external.dto.attachment.toDomain

class AttachmentResponseTest : ShouldSpec({

    should("map correctly to Attachment") {
        // given
        val attachmentResponse = AttachmentResponse(id = "123")

        // when
        val attachment = attachmentResponse.toDomain()

        // then
        attachment.also {
            it.id shouldBe attachmentResponse.id
        }
    }
},)
