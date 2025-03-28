package com.gnome.gnome.annotations.config;

import java.lang.annotation.*;

/**
 * Annotation used to mark a field or constructor parameter
 * for value injection from a properties file.
 * <p>
 * When applied, the {@code value()} should match a key in the loaded
 * configuration (e.g., {@code app.properties}).
 * <p>
 * This annotation can be used for:
 * <ul>
 *     <li>Field injection</li>
 *     <li>Constructor parameter injection</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * @Value("app.url")
 * private String url;
 *
 * public MyClass(@Value("app.timeout") int timeout) {
 *     this.timeout = timeout;
 * }
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    /**
     * The key to look up in the properties file.
     *
     * @return the name of the property to inject
     */
    String value();
}
