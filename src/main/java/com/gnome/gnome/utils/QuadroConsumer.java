package com.gnome.gnome.utils;

@FunctionalInterface
public interface QuadroConsumer<A, B, C, D> {
    void accept(A a, B b, C c, D d);
}