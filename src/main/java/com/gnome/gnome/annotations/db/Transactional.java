package com.gnome.gnome.annotations.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should be executed within a database transaction.
 * <p>
 * This annotation is typically used to wrap the method's logic with:
 * <ul>
 *     <li>{@code beginTransaction()} before the method executes</li>
 *     <li>{@code commitTransaction()} if the method completes successfully</li>
 *     <li>{@code rollBackTransaction()} if an exception is thrown</li>
 * </ul>
 *
 * <p>It requires a supporting aspect or interceptor to handle transaction logic.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Transactional
 * public void updateUser(User user) {
 *     // logic here
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transactional {
}
