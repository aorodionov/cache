package ru.company;

public interface AbstractMetadataFactory<M extends Metadata> {
    Metadata getInstance();
}
