package ru.company;

/**
 * Class containing information for process of invalidation.
 */
public interface Metadata<T> extends Comparable<T> {
    /**
     * Renew state of information.
     */
    void update();

    /**
     * Init method
     *
     * @return defaulted data
     */
    Metadata initialFill();
}
