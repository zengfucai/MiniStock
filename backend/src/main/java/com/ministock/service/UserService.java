package com.ministock.service;

import com.ministock.entity.Account;
import com.ministock.entity.FundRecord;
import com.ministock.entity.User;
import com.ministock.mapper.AccountMapper;
import com.ministock.mapper.FundRecordMapper;
import com.ministock.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final FundRecordMapper fundRecordMapper;

    public UserService(
            UserMapper userMapper,
            AccountMapper accountMapper,
            FundRecordMapper fundRecordMapper
    ) {
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
        this.fundRecordMapper = fundRecordMapper;
    }

    public List<User> findAll() {
        List<User> users = userMapper.findAll();

        for (User user : users) {
            user.setPassword(null);
        }

        return users;
    }

    public User findById(Long id) {
        User user = userMapper.findById(id);

        if (user != null) {
            user.setPassword(null);
        }

        return user;
    }

    public Account getAccount(Long userId) {
        return accountMapper.findByUserId(userId);
    }

    @Transactional
    public void updateStatus(Long userId, String status) {

        if (!"NORMAL".equals(status)
                && !"DISABLED".equals(status)) {
            throw new RuntimeException("状态只能是NORMAL或DISABLED");
        }

        if (userMapper.findById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }

        userMapper.updateStatus(userId, status);
    }

    @Transactional
    public void changeBalance(
            Long userId,
            BigDecimal amount,
            String description
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("金额不能为null或0");
        }

        Account account =
                accountMapper.findByUserId(userId);

        if (account == null) {
            throw new RuntimeException("账户不存在");
        }

        int result;
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            result = accountMapper.addBalance(
                    userId,
                    amount
            );
        } else {
            result = accountMapper.subtractBalance(
                    userId,
                    amount.abs()
            );
        }

        if (result == 0) {
            throw new RuntimeException("余额操作失败");
        }

        Account newAccount =
                accountMapper.findByUserId(userId);
        FundRecord record = new FundRecord();
        record.setUserId(userId);
        record.setType("ADMIN_ADJUST");
        record.setAmount(amount);
        record.setBalanceAfter(
                newAccount.getBalance()
        );
        record.setDescription(description);

        fundRecordMapper.insert(record);
    }

    public List<Account> findAllAccounts() {
        return accountMapper.findAll();
    }

    public List<FundRecord> findAllFundRecords() {
        return fundRecordMapper.findAll();
    }

    public List<FundRecord> findUserFundRecords(Long userId) {
        return fundRecordMapper.findByUserId(userId);
    }
}
