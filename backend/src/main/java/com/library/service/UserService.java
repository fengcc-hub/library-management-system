package com.library.service;

import com.library.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    /** User login: returns JWT token and user info */
    Map<String, Object> login(String username, String password);

    /** Register a new reader */
    User register(User user);

    /** Paginated user list */
    Map<String, Object> getUserPage(int pageNum, int pageSize, String keyword);

    /** Get user by id */
    User getUserById(Long id);

    /** Add user (admin) */
    User addUser(User user);

    /** Update user */
    User updateUser(User user);

    /** Delete user (logical) */
    void deleteUser(Long id);

    /** Reset password */
    void resetPassword(Long id, String newPassword);

    /** Get all readers */
    List<User> getAllReaders();

    /** Get current logged-in user info */
    User getCurrentUser(String username);
}
