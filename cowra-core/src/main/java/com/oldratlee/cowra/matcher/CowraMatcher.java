package com.oldratlee.cowra.matcher;

/**
 * @author ding.lid
 */
public interface CowraMatcher {
    /**
     * @return {@code true} if class className match pattern, otherwise {@code false}
     */
    boolean match(String className, String pattern);

    /**
     * Validate pattern.
     *
     * @throws IllegalStateException if pattern is illegal.
     */
    boolean validate(String pattern);
}
