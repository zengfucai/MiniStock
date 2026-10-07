package com.ministock.controller;

import com.ministock.service.TradeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/apply")
    public Map<String, Object> apply(
            @RequestBody Map<String, Object> body,
            HttpSession session
    ) {

        Long userId = getUserId(session);

        Long stockId =
                Long.valueOf(
                        body.get("stockId").toString()
                );

        String type =
                body.get("type").toString();

        Integer quantity =
                Integer.valueOf(
                        body.get("quantity").toString()
                );

        tradeService.createApplication(
                userId,
                stockId,
                type,
                quantity
        );

        return success(null);
    }

    @GetMapping("/applications")
    public Map<String, Object> applications(
            HttpSession session
    ) {

        checkAdmin(session);

        return success(
                tradeService.getAllApplications()
        );
    }

    @PostMapping("/approve/{id}")
    public Map<String, Object> approve(
            @PathVariable Long id,
            HttpSession session
    ) {

        checkAdmin(session);

        tradeService.approve(id);

        return success(null);
    }

    @PostMapping("/reject/{id}")
    public Map<String, Object> reject(
            @PathVariable Long id,
            @RequestBody(required = false)
            Map<String, String> body,
            HttpSession session
    ) {

        checkAdmin(session);

        String reason = "????????";

        if (body != null
                && body.get("reason") != null) {
            reason = body.get("reason");
        }

        tradeService.reject(id, reason);

        return success(null);
    }

    @GetMapping("/records")
    public Map<String, Object> records(
            HttpSession session
    ) {

        checkAdmin(session);

        return success(
                tradeService.getAllTradeRecords()
        );
    }

    private Long getUserId(HttpSession session) {

        Object userId =
                session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("?????");
        }

        return Long.valueOf(
                userId.toString()
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