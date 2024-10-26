package pl.edu.agh.gem.external.dto.attachment

import pl.edu.agh.gem.internal.model.finance.Attachment

data class AttachmentResponse(
    val attachmentId:String
)

fun AttachmentResponse.toDomain() =
        Attachment(
                attachmentId = attachmentId
        )
