package test

import java.util.*

fun printStream() = System.out
fun list() = Collections.emptyList<String>()  // return type is externally annotated with org.jetbrains.annotation.ReadOnly
fun array(a: Array<Int>) = Arrays.copyOf(a, 2)
