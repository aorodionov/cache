package ru.company;

/**
 * Factory of {@link SimpleMetadata} for {@link SimpleInvalidator}
 */
public class SimpleMetadataFactory implements AbstractMetadataFactory {
    @Override
    public Metadata getInstance() {
        Metadata metadata = new SimpleMetadata();
        metadata.initialFill();
        return metadata;
    }
}
