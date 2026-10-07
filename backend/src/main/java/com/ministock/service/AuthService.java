package com.ministock.service;

import com.ministock.entity.Account;
import com.ministock.entity.User;
import com.ministock.mapper.AccountMapper;
import com.ministock.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AuthService(
            UserMapper userMapper,
            AccountMapper accountMapper
    ) {
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public User register(
            String username,
            String password
    ) {

        if (username == null || username.isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }

        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码不能少于6位");
        }

        if (userMapper.findByUsername(username) != null) {
            throw new RuntimeException("用户名已经存在");
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(
                encoder.encode(password)
        );
        user.setRole("USER");
        user.setStatus("NORMAL");

        userMapper.insert(user);

        Account account = new Account();

        account.setUserId(user.getId());
        account.setBalance(
                new BigDecimal("100000.00")
        );

        accountMapper.insert(account);

        user.setPassword(null);

        return user;
    }

    public User login(
            String username,
            String password
    ) {

        if (username == null || username.isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        User user =
                userMapper.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!"NORMAL".equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用");
        }

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {

            throw new RuntimeException("账号密码数据异常");
        }

        boolean matched =
                encoder.matches(
                        password,
                        user.getPassword()
                );

        if (!matched) {
            throw new RuntimeException("用户名或密码错误");
        }

        user.setPassword(null);

        return user;
    }

    public User checkLoginUser(Long userId) {

        if (userId == null) {
            return null;
        }

        return userMapper.findById(userId);
    }
}