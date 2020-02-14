package com.example.graphql.domain.payment

import spock.lang.Specification

class PaymentServiceTest extends Specification {

    def "Should #shouldChange change payment status when status is changed from #statusFrom to #statusTo"() {
        expect:
//        ACCEPTED,
//        DECLINED,
//        IN_PROGRESS,
//        PAID,
//        CONFIRMED
        false
        where:
        statusFrom                | statusTo                  | shouldChange
        PaymentStatus.IN_PROGRESS | PaymentStatus.ACCEPTED    | true
        PaymentStatus.IN_PROGRESS | PaymentStatus.DECLINED    | true
        PaymentStatus.PAID        | PaymentStatus.DECLINED    | true
        PaymentStatus.PAID        | PaymentStatus.CONFIRMED   | true
        PaymentStatus.ACCEPTED    | PaymentStatus.DECLINED    | true
        PaymentStatus.ACCEPTED    | PaymentStatus.PAID        | true
        PaymentStatus.DECLINED    | PaymentStatus.ACCEPTED    | true

        PaymentStatus.IN_PROGRESS | PaymentStatus.CONFIRMED   | false
        PaymentStatus.IN_PROGRESS | PaymentStatus.PAID        | false
        PaymentStatus.ACCEPTED    | PaymentStatus.CONFIRMED   | false
        PaymentStatus.ACCEPTED    | PaymentStatus.IN_PROGRESS | false
        PaymentStatus.DECLINED    | PaymentStatus.PAID        | false
        PaymentStatus.DECLINED    | PaymentStatus.CONFIRMED   | false
        PaymentStatus.DECLINED    | PaymentStatus.IN_PROGRESS | false
        PaymentStatus.PAID        | PaymentStatus.ACCEPTED    | false
        PaymentStatus.PAID        | PaymentStatus.IN_PROGRESS | false
        PaymentStatus.CONFIRMED   | PaymentStatus.IN_PROGRESS | false
        PaymentStatus.CONFIRMED   | PaymentStatus.PAID        | false
        PaymentStatus.CONFIRMED   | PaymentStatus.DECLINED    | false
        PaymentStatus.CONFIRMED   | PaymentStatus.ACCEPTED    | false
    }
}
