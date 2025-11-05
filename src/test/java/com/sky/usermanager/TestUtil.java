package com.sky.usermanager;

import com.sky.usermanager.model.User;

public class TestUtil {

    public static final String JOHN_DOE_NAME = "John Doe";
    public static final String JOHN_EXAMPLE_EMAIL = "john@example.com";

    public static final String TEST_EXAMPLE_EMAIL = "test@example.com";
    public static final String HASHED_PASSWORD_123 = "hashedPassword123";

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    public static void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static User mockUser() {
        User user = new User("john.doe@example.com", "hashed_password", JOHN_DOE_NAME);

        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return user;
    }

}
