package ru.company;

public class SimpleMetadataFactory implements AbstractMetadataFactory {
    @Override
    public Metadata getInstance() {
        Metadata metadata = new SimpleMetadata();
        metadata.initialFill();
        return metadata;
    }
}
