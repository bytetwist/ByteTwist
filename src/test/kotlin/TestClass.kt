class TestClass {

    var testField1 = 1000

    var testField2 = 0

    fun testMethod1() {
        if (testField1 == 0) {
            testMethod2()
        }
        testField1 = 0
        if (testField1 == testField2) {
            testField1 = 6
        }
    }

    fun testMethod2() {
        testField2 * 1
    }

}