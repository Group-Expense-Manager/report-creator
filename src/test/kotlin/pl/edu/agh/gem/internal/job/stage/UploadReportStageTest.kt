package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.bson.types.Binary
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.client.RetryableAttachmentStoreClientException
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELECTION
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.util.createAttachment
import pl.edu.agh.gem.util.createReportJob

class UploadReportStageTest : ShouldSpec({

    val attachmentStoreClient = mock(AttachmentStoreClient::class.java)
    val uploadReportStage = spy(UploadReportStage(attachmentStoreClient))

    should("proceed to FORMAT_SELECTION state when file is null") {
        // given
        val reportJob = createReportJob(file = null)

        // when
        uploadReportStage.process(reportJob)

        // then
        verify(uploadReportStage).nextStage(reportJob, FORMAT_SELECTION)
    }

    should("upload attachment and proceed to SAVING state when file is not null") {
        // given
        val reportJob = createReportJob(file = Binary(ByteArray(0)))
        val attachment = createAttachment("attachmentId")
        whenever(attachmentStoreClient.uploadAttachment(reportJob.groupId, reportJob.creatorId, reportJob.file!!))
            .thenReturn(attachment)

        // when
        uploadReportStage.process(reportJob)

        // then
        verify(attachmentStoreClient).uploadAttachment(reportJob.groupId, reportJob.creatorId, reportJob.file!!)
        verify(uploadReportStage).nextStage(reportJob.copy(attachmentId = attachment.id), SAVING)
    }

    should("retry on RetryableAttachmentStoreClientException") {
        // given
        val reportJob = createReportJob(file = Binary(ByteArray(0)))
        whenever(attachmentStoreClient.uploadAttachment(reportJob.groupId, reportJob.creatorId, reportJob.file!!))
            .thenThrow(RetryableAttachmentStoreClientException::class.java)

        // when
        uploadReportStage.process(reportJob)

        // then
        verify(attachmentStoreClient).uploadAttachment(reportJob.groupId, reportJob.creatorId, reportJob.file!!)
        verify(uploadReportStage).retry()
    }
},)
