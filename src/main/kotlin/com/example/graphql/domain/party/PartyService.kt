package com.example.graphql.domain.party

import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class PartyService {
    fun getTestParty(): Party {
        return Party(
                name = "test name",
                startDate = ZonedDateTime.now()
        )
    }
}
