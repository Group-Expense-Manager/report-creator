package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.user.UsersDetails

interface UserDetailsManagerClient {
    fun getUsersDetails(groupId: String): UsersDetails
}

class UserDetailsManagerClientException(override val message: String?) : RuntimeException()

class RetryableUserDetailsManagerClientException(override val message: String?) : RuntimeException()
