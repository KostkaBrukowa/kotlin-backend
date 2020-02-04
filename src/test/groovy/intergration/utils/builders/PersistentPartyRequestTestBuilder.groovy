package intergration.utils.builders

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.PersistentUser

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames
import static intergration.utils.builders.PersistentPartyTestBuilder.defaultPersistentParty
import static intergration.utils.builders.PersistentUserTestBuilder.defaultPersistentUser

class PersistentPartyRequestTestBuilder {

    private static def defaults = [
            id    : '0',
            user  : null,
            party : null,
            status: PartyRequestStatus.IN_PROGRESS,
    ]

    private PersistentPartyRequestTestBuilder() {}

    static PersistentPartyRequest defaultPersistentPartyRequest(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentPartyRequest(
                allArgs.id as Long,
                allArgs.status as PartyRequestStatus,
                allArgs.user as PersistentUser,
                allArgs.party as PersistentParty,
        )
    }

    static PersistentPartyRequest aPartyRequest(Map props = [:], PersistentPartyRequestRepository repository) {
        return repository.save(defaultPersistentPartyRequest(props))
    }
}
