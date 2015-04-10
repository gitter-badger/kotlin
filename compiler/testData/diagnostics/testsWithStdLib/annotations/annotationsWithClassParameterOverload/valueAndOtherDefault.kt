// FILE: A.java
public @interface A {
    Class<?> value();
    int x() default 1;
}

// FILE: b.kt
A(javaClass<String>()) class MyClass1
A(String::class) class MyClass2
A(value = javaClass<String>()) class MyClass3
A(value = String::class) class MyClass4

A(javaClass<String>(), x = 1) class MyClass5
A(String::class, x = 2) class MyClass6
A(value = javaClass<String>(), x = 3) class MyClass7
A(value = String::class, x = 4) class MyClass8
