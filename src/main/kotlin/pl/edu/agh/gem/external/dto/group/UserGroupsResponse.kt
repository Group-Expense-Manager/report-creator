package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.group.Group

data class UserGroupsResponse(
    val groups: List<GroupDTO>,
) {
    fun toDomain() = groups.map { Group(it.groupId) }
}

data class GroupDTO(
    val groupId: String,
)
