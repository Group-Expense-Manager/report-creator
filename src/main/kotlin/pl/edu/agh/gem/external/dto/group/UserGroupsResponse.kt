package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.group.Group

data class UserGroupsResponse(
    val groups: List<GroupDTO>,
)

data class GroupDTO(
    val groupId: String,
)

fun UserGroupsResponse.toDomain() = groups.map { Group(it.groupId) }
