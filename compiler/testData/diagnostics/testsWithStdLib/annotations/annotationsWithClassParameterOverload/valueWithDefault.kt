// !DIAGNOSTICS: -JAVA_LANG_CLASS_ARGUMENT_IN_ANNOTATION
// FILE: A.java
public @interface A {
    Class<?> value() default Integer.class;
}

// FILE: b.kt
A(javaClass<String>()) class MyClass1
A(String::class) class MyClass2
A(value = javaClass<String>()) class MyClass3
A(value = String::class) class MyClass4
A class MyClass5
