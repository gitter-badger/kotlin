enum class A(val b: String) {
    E1: A("OK"){ override fun t() = b }

    abstract fun t(): String
}

fun box()= A.E1.t()
