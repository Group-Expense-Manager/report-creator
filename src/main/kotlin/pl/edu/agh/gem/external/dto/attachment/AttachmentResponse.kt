package pl.edu.agh.gem.external.dto.attachment

import pl.edu.agh.gem.internal.model.finance.Attachment

data class AttachmentResponse(
    val id: String,
)

fun AttachmentResponse.toDomain() =
    Attachment(
        id = id,
    )
