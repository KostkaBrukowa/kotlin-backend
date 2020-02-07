package com.example.graphql.domain.party

import com.example.graphql.schema.exceptions.handlers.UnauthorisedException


fun requirePartyOwner(party: Party, currentUserId: Long) {
    if (party.owner == null) throw InternalError("Party owner was not fetched from DB")
    if (party.owner.id != currentUserId) throw UnauthorisedException()
}
