package pl.edu.agh.gem.internal.model.group

import pl.edu.agh.gem.model.GroupMember

data class GroupDetails(
    val groupId: String,
    val members: List<GroupMember>,
    val groupCurrencies: List<Currency>,
    val name: String,
    val attachmentId: String,
)

data class Currency(
    val code: String,
)
