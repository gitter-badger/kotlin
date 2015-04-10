// !DIAGNOSTICS: -JAVA_LANG_CLASS_ARGUMENT_IN_ANNOTATION
// FILE: A.java
public @interface A {
    Class<?> value() default Integer.class;
    int x();
}

// FILE: b.kt
A(javaClass<String>(), x = 1) class MyClass1
A(String::class, x = 2) class MyClass2
A(value = javaClass<String>(), x = 3) class MyClass3
A(value = String::class, x = 4) class MyClass4
A(x = 5) class MyClass5
