// !DIAGNOSTICS: -JAVA_LANG_CLASS_ARGUMENT_IN_ANNOTATION
// FILE: A.java
public @interface A {
    Class<?>[] value();
}

// FILE: b.kt
A(javaClass<String>(), javaClass<Int>()) class MyClass1
A(String::class, Int::class) class MyClass2

A(*array(javaClass<String>(), javaClass<Int>())) class MyClass3
A(*array(String::class, Int::class)) class MyClass4

A(value = *array(javaClass<String>(), javaClass<Int>())) class MyClass5
A(value = *array(String::class, Int::class)) class MyClass6

A(<!TYPE_MISMATCH!>javaClass<String>()<!>, Int::class) class MyClass7
A(String::class, <!TYPE_MISMATCH!>javaClass<Int>()<!>) class MyClass8
