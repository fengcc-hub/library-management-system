package com.library.controller;

import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Authentication Controller
 * - Login (JWT token generation)
 * - Register (new reader)
 * - Get current user info
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        Map<String, Object> result = userService.login(username, password);
        return Result.success("登录成功", result);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User result = userService.register(user);
        return Result.success("注册成功", result);
    }

    @GetMapping("/info")
    public Result<User> getCurrentUser(Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, Authentication authentication) {
        // JWT is stateless, client just discards the token
        // Optionally invalidate Redis token here
        return Result.success();
    }
}
