// !DIAGNOSTICS: -JAVA_LANG_CLASS_ARGUMENT_IN_ANNOTATION
// FILE: A.java
public @interface A {
    Class<?> arg();
    int x() default 1;
}

// FILE: b.kt
A(arg = javaClass<String>()) class MyClass1
A(arg = String::class) class MyClass2

A(arg = javaClass<String>(), x = 1) class MyClass3
A(arg = String::class, x = 2) class MyClass4
