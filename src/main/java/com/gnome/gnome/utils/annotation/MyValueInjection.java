package com.gnome.gnome.utils.annotation;

import com.gnome.gnome.annotations.config.Value;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Custom lightweight dependency injection utility for injecting property values into fields or constructor parameters
 * annotated with {@link Value}.
 * <p>
 * It supports:
 * <ul>
 *     <li>Constructor-based injection for final fields or immutable objects</li>
 *     <li>Field-based injection for mutable non-final fields</li>
 * </ul>
 *
 * Properties are loaded from the {@code app.properties} file placed in the classpath root.
 *
 * Example usage:
 * <pre>{@code
 * public class MyService {
 *     @Value("app.name")
 *     private String appName;
 *
 *     public MyService(@Value("app.url") String url) {
 *         ...
 *     }
 * }
 *
 * MyService service = MyValueInjection.getInstance().createInstance(MyService.class);
 * }</pre>
 */
public class MyValueInjection {

    // Logger instance for logging messages.
    private static final Logger logger = Logger.getLogger(MyValueInjection.class.getName());

    // Properties loaded from the app.properties file.
    private final Properties properties = new Properties();

    // Singleton instance of MyValueInjection.
    private static MyValueInjection injection;

    /**
     * Private constructor that loads {@code app.properties} from the classpath.
     * Throws a runtime exception if the file cannot be loaded.
     */
    private MyValueInjection() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find app.properties in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
    }

    /**
     * Returns the singleton instance of {@code MyValueInjection}.
     *
     * @return the injection utility instance
     */
    public static MyValueInjection getInstance() {
        if (injection == null) {
            injection = new MyValueInjection();
        }
        return injection;
    }

    /**
     * Creates an instance of the given class, injecting values into constructor parameters or fields
     * annotated with {@link Value}.
     * <p>
     * If a constructor exists with all parameters annotated using {@code @Value}, that constructor will be used.
     * Otherwise, a no-arg constructor is used followed by field injection.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the class
     * @return a new instance of the class with injected values
     */
    public <T> T createInstance(Class<T> clazz) {
        try {
            // Retrieve all declared constructors for the given class.
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> selectedConstructor = null;
            Object[] constructorArgs = null;

            // Look for a constructor where all parameters have the @Value annotation.
            for (Constructor<?> constructor : constructors) {
                Parameter[] parameters = constructor.getParameters();
                if (Arrays.stream(parameters).allMatch(p -> p.isAnnotationPresent(Value.class))) {
                    // For each parameter, fetch the property value and convert it to the required type.
                    constructorArgs = Arrays.stream(parameters)
                            .map(p -> {
                                String key = p.getAnnotation(Value.class).value();
                                String raw = properties.getProperty(key);
                                return convert(raw, p.getType());
                            })
                            .toArray();

                    selectedConstructor = constructor;
                    break;
                }
            }

            T instance;

            // If a constructor with all @Value parameters is found, use it for instance creation.
            if (selectedConstructor != null) {
                selectedConstructor.setAccessible(true);
                instance = (T) selectedConstructor.newInstance(constructorArgs);
            } else {
                // Fallback to default constructor
                Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
                defaultConstructor.setAccessible(true);
                instance = defaultConstructor.newInstance();
            }

            // After construction, perform field-based injection
            injectFieldValues(instance);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of class: " + clazz.getName(), e);
        }
    }

    /**
     * Injects values into all non-final fields of the target object annotated with {@link Value}.
     *
     * @param target the target object to inject values into
     */
    private void injectFieldValues(Object target) {
        // Iterate over each declared field of the object's class.
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                // Retrieve the property key from the annotation.
                Value annotation = field.getAnnotation(Value.class);
                String key = annotation.value();
                String raw = properties.getProperty(key);

                // Only inject if the property exists and the field is not marked as final.
                if (raw != null && !Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        // Convert the string property to the field's type and set the field's value.
                        Object converted = convert(raw, field.getType());
                        field.set(target, converted);
                    } catch (IllegalAccessException e) {
                        // Log the error and rethrow as a runtime exception.
                        logger.severe("Failed to inject value into field: " + field.getName());
                        throw new RuntimeException("Failed to inject value into field: " + field.getName(), e);
                    }
                }
            }
        }
    }

    /**
     * Converts a string value from the properties file to the correct type based on the field or parameter type.
     *
     * @param value the string value from the properties file
     * @param type  the target type
     * @return the converted object
     */
    private Object convert(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        return value;
    }
}
