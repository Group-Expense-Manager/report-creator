package pl.edu.agh.gem.external.dto.group

import pl.edu.agh.gem.internal.model.group.Currency
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.model.GroupMember

data class GroupResponse(
    val groupId: String,
    val members: List<MemberDto>,
    val groupCurrencies: List<CurrencyDto>,
    val name: String,
    val attachmentId: String,
)

data class MemberDto(
    val id: String,
)

data class CurrencyDto(
    val code: String,
)

fun GroupResponse.toDomain() =
    GroupDetails(
        groupId = groupId,
        members = members.map { GroupMember(it.id) },
        groupCurrencies = groupCurrencies.map { Currency(it.code) },
        name = name,
        attachmentId = attachmentId,
    )
