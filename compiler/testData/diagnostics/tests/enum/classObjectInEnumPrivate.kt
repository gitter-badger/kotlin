enum class E {
    ENTRY

    private companion object
}

fun foo() = E.values()
fun bar() = E.valueOf("ENTRY")
fun baz() = E.ENTRY
fun quux() = <!INVISIBLE_MEMBER!>E<!>
