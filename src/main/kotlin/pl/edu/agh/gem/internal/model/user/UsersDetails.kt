package pl.edu.agh.gem.internal.model.user

data class UserDetails(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
)

class UsersDetails(
    val users: Map<String, UserDetails>,
) {
    private fun getUserDetails(userId: String): UserDetails {
        return users[userId] ?: throw UserNotFoundException(userId)
    }

    fun getUserName(userId: String): String {
        val user = getUserDetails(userId)
        return listOf(user.firstName, user.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: user.username
    }

    class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
}
