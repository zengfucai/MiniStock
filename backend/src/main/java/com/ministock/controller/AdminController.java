package com.ministock.controller;

import com.ministock.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Map<String, Object> users(
            HttpSession session
    ) {

        checkAdmin(session);

        return success(
                userService.findAll()
        );
    }

    @GetMapping("/accounts")
    public Map<String, Object> accounts(
            HttpSession session
    ) {

        checkAdmin(session);

        return success(
                userService.findAllAccounts()
        );
    }

    @PostMapping("/users/{id}/status")
    public Map<String, Object> status(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        checkAdmin(session);

        userService.updateStatus(
                id,
                body.get("status")
        );

        return success(null);
    }

    @PostMapping("/users/{id}/balance")
    public Map<String, Object> balance(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        checkAdmin(session);

        BigDecimal amount =
                new BigDecimal(
                        body.get("amount")
                );

        userService.changeBalance(
                id,
                amount,
                body.get("description")
        );

        return success(null);
    }

    @GetMapping("/fund-records")
    public Map<String, Object> fundRecords(
            HttpSession session
    ) {

        checkAdmin(session);

        return success(
                userService.findAllFundRecords()
        );
    }

    private void checkAdmin(HttpSession session) {

        if (!"ADMIN".equals(
                session.getAttribute("role")
        )) {
            throw new RuntimeException("???????????");
        }
    }

    private Map<String, Object> success(Object data) {

        Map<String, Object> result =
                new HashMap<>();

        result.put("success", true);
        result.put("data", data);

        return result;
    }
}