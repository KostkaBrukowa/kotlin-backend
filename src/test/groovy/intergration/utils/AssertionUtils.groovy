package intergration.utils

class AssertionUtils {

    static void assertUnauthorizedError(ArrayList response) {
        assert response[0].errorType == 'DataFetchingException'
        assert response[0].message.contains('User is not authorised to perform this action')
    }
}
