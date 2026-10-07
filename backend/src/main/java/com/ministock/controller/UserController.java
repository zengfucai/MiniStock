package com.ministock.controller;

import com.ministock.entity.Account;
import com.ministock.entity.FundRecord;
import com.ministock.entity.Position;
import com.ministock.entity.User;
import com.ministock.service.TradeService;
import com.ministock.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TradeService tradeService;

    public UserController(
            UserService userService,
            TradeService tradeService
    ) {
        this.userService = userService;
        this.tradeService = tradeService;
    }

    @GetMapping
    public Map<String, Object> findAll() {

        List<User> users =
                userService.findAll();

        return success(users);
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(
            @PathVariable Long id
    ) {

        User user =
                userService.findById(id);

        if (user == null) {
            return fail("用户不存在");
        }

        return success(user);
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(
            HttpSession session
    ) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return fail("未登录");
        }

        User user =
                userService.findById(userId);

        if (user == null) {
            return fail("用户不存在");
        }

        Account account =
                userService.getAccount(userId);

        if (account == null) {
            return fail("账户不存在");
        }

        List<Position> positions =
                tradeService.getPositions(userId);

        Map<String, Object> data =
                new HashMap<>();

        data.put("user", user);
        data.put("account", account);
        data.put("positions", positions);

        return success(data);
    }

    @GetMapping("/account")
    public Map<String, Object> getAccount(
            HttpSession session
    ) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return fail("未登录");
        }

        Account account =
                userService.getAccount(userId);

        if (account == null) {
            return fail("账户不存在");
        }

        return success(account);
    }

    @GetMapping("/applications")
    public Map<String, Object> getApplications(
            HttpSession session
    ) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return fail("未登录");
        }

        return success(
                tradeService.getApplications(userId)
        );
    }

    @GetMapping("/trades")
    public Map<String, Object> getTrades(
            HttpSession session
    ) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return fail("未登录");
        }

        return success(
                tradeService.getTradeRecords(userId)
        );
    }

    @GetMapping("/fund-records")
    public Map<String, Object> getFundRecords(
            HttpSession session
    ) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return fail("未登录");
        }

        List<FundRecord> records =
                userService.findUserFundRecords(userId);

        return success(records);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        String status =
                body.get("status");

        userService.updateStatus(
                id,
                status
        );

        return success(null);
    }

    @PutMapping("/{id}/balance")
    public Map<String, Object> changeBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {

        Object amountObject =
                body.get("amount");

        if (amountObject == null) {
            return fail("金额不能为空");
        }

        BigDecimal amount;

        try {

            amount =
                    new BigDecimal(
                            amountObject.toString()
                    );

        } catch (NumberFormatException e) {

            return fail("金额格式错误");
        }

        String description =
                body.get("description") == null
                        ? "管理员资金调整"
                        : body.get("description").toString();

        userService.changeBalance(
                id,
                amount,
                description
        );

        return success(null);
    }

    @GetMapping("/admin/accounts")
    public Map<String, Object> findAllAccounts() {

        List<Account> accounts =
                userService.findAllAccounts();

        return success(accounts);
    }

    @GetMapping("/admin/fund-records")
    public Map<String, Object> findAllFundRecords() {

        List<FundRecord> records =
                userService.findAllFundRecords();

        return success(records);
    }

    private Long getUserId(
            HttpSession session
    ) {

        Object userId =
                session.getAttribute("userId");

        if (userId == null) {
            return null;
        }

        if (userId instanceof Long) {
            return (Long) userId;
        }

        return Long.valueOf(
                userId.toString()
        );
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

    private Map<String, Object> fail(
            String message
    ) {

        Map<String, Object> result =
                new HashMap<>();

        result.put("success", false);
        result.put("message", message);

        return result;
    }
}