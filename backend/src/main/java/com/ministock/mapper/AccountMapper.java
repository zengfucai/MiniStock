package com.ministock.mapper;

import com.ministock.entity.Account;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AccountMapper {

    @Select("""
        SELECT *
        FROM accounts
        WHERE user_id = #{userId}
        """)
    Account findByUserId(Long userId);

    @Insert("""
        INSERT INTO accounts(user_id, balance)
        VALUES(#{userId}, #{balance})
        """)
    int insert(Account account);

    @Update("""
        UPDATE accounts
        SET balance = balance + #{amount}
        WHERE user_id = #{userId}
        """)
    int addBalance(@Param("userId") Long userId,
                   @Param("amount") BigDecimal amount);

    @Update("""
        UPDATE accounts
        SET balance = balance - #{amount}
        WHERE user_id = #{userId}
          AND balance >= #{amount}
        """)
    int subtractBalance(@Param("userId") Long userId,
                        @Param("amount") BigDecimal amount);

    @Select("""
        SELECT a.*, u.username
        FROM accounts a
        JOIN users u ON u.id = a.user_id
        ORDER BY a.user_id
        """)
    List<Account> findAll();
}