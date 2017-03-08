package ru.company;

public interface Metadata<T> extends Comparable<T> {
    void update();

    Metadata newInstance();
}
