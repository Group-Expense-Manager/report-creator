package pl.edu.agh.gem.external.dto.user

import pl.edu.agh.gem.internal.model.user.UserDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails

data class GroupUsersDetailsResponse(
    val details: List<UserDetailsDto>,
)

data class UserDetailsDto(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
)

fun GroupUsersDetailsResponse.toDomain() = UsersDetails(
    details.associate { it.id to it.toUserDetails() },
)

fun UserDetailsDto.toUserDetails() = UserDetails(
    id = id,
    username = username,
    firstName = firstName,
    lastName = lastName,
)
