package com.ministock.controller;

import com.ministock.entity.Stock;
import com.ministock.service.StockService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public Map<String, Object> list() {
        return success(stockService.findAll());
    }

    @GetMapping("/{id}")
    public Map<String, Object> find(
            @PathVariable Long id
    ) {

        return success(
                stockService.findById(id)
        );
    }

    @PostMapping
    public Map<String, Object> save(
            @RequestBody Stock stock,
            HttpSession session
    ) {

        checkAdmin(session);

        stockService.save(stock);

        return success(null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @PathVariable Long id,
            HttpSession session
    ) {

        checkAdmin(session);

        stockService.delete(id);

        return success(null);
    }

    private void checkAdmin(HttpSession session) {

        Object role =
                session.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("没有管理员权限");
        }
    }

    private Map<String, Object> success(
            Object data
    ) {

        Map<String, Object> result =
                new HashMap<>();

        result.put("success", true);
        result.put("data", data);

        return result;
    }
}