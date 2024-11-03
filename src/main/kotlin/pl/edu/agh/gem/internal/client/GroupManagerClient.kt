package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.group.Group
import pl.edu.agh.gem.internal.model.group.GroupDetails

interface GroupManagerClient {
    fun getGroups(userId: String): List<Group>
    fun getGroupDetails(groupId: String): GroupDetails
}

class GroupManagerClientException(override val message: String?) : RuntimeException()

class RetryableGroupManagerClientException(override val message: String?) : RuntimeException()
