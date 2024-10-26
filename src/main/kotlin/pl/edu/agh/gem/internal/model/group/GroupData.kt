package pl.edu.agh.gem.internal.model.group

import pl.edu.agh.gem.model.GroupMember

data class GroupData(
    val members: List<GroupMember>,
    val currencies: List<Currency>,
)

data class Currency(
    val code: String,
)
