// FILE: A.java
public @interface A {
    Class<?> arg() default Integer.class;
    int x();
}

// FILE: b.kt
A(arg = javaClass<String>(), x = 3) class MyClass1
A(arg = String::class, x = 4) class MyClass2
A(x = 5) class MyClass3
