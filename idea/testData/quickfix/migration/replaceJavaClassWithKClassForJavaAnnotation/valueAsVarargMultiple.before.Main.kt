// "Replace javaClass<T>() with T::class" "true"
// WITH_RUNTIME

Ann(
javaClass<String>(),
javaClass<Int>(),
*array(javaClass<Double>()),
x = 2,
arg = javaClass<Int>(),
args = array(javaClass<Any>(), javaClass<java.lang.String>())<caret>)
class MyClass
