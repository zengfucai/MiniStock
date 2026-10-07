package com.ministock.controller;

import com.ministock.entity.User;
import com.ministock.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestBody Map<String, String> body
    ) {

        try {

            User user =
                    authService.register(
                            body.get("username"),
                            body.get("password")
                    );

            return success(user);

        } catch (RuntimeException e) {

            return fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        try {

            User user =
                    authService.login(
                            body.get("username"),
                            body.get("password")
                    );

            session.setAttribute(
                    "userId",
                    user.getId()
            );

            session.setAttribute(
                    "role",
                    user.getRole()
            );

            return success(user);

        } catch (RuntimeException e) {

            return fail(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(
            HttpSession session
    ) {

        session.invalidate();

        return success(null);
    }

    @GetMapping("/me")
    public Map<String, Object> me(
            HttpSession session
    ) {

        Object userId =
                session.getAttribute("userId");

        if (userId == null) {
            return fail("未登录");
        }

        try {

            Long id =
                    Long.valueOf(
                            userId.toString()
                    );

            User user =
                    authService.checkLoginUser(id);

            if (user == null) {

                session.invalidate();

                return fail("账号不存在");
            }

            if (!"NORMAL".equals(user.getStatus())) {

                session.invalidate();

                return fail("账号已被禁用");
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "userId",
                    user.getId()
            );

            data.put(
                    "username",
                    user.getUsername()
            );

            data.put(
                    "role",
                    user.getRole()
            );

            data.put(
                    "status",
                    user.getStatus()
            );

            return success(data);

        } catch (RuntimeException e) {

            session.invalidate();

            return fail(e.getMessage());
        }
    }

    private Map<String, Object> success(
            Object data
    ) {

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "success",
                true
        );

        result.put(
                "data",
                data
        );

        return result;
    }

    private Map<String, Object> fail(
            String message
    ) {

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "success",
                false
        );

        result.put(
                "message",
                message == null
                        ? "操作失败"
                        : message
        );

        return result;
    }
}