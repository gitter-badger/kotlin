package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

object TestObject {
    var instanceof: Int = 0

    fun test() {
        testNotRenamed("instanceof", { instanceof })
    }
}

fun box(): String {
    TestObject.test()

    return "OK"
}