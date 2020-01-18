package com.example.graphql.utils

class VerifyingBuilder {

    static def verifyPropertyNames(Map defaults, Map properties) {
        def allowedPropertyNames = defaults.keySet()
        def candidatePropertyNames = properties.keySet()

        if (!(candidatePropertyNames - allowedPropertyNames).empty) {
            throw new IllegalArgumentException("Validation failed. Unknown properties: " + candidatePropertyNames.minus(allowedPropertyNames))
        }
    }
}
