package com.ministock.mapper;

import com.ministock.entity.TradeApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TradeApplicationMapper {

    @Select("""
        SELECT ta.*,
               u.username,
               s.code AS stock_code,
               s.name AS stock_name
        FROM trade_applications ta
        JOIN users u ON u.id = ta.user_id
        JOIN stocks s ON s.id = ta.stock_id
        WHERE ta.user_id = #{userId}
        ORDER BY ta.id DESC
        """)
    List<TradeApplication> findByUserId(Long userId);

    @Select("""
        SELECT ta.*,
               u.username,
               s.code AS stock_code,
               s.name AS stock_name
        FROM trade_applications ta
        JOIN users u ON u.id = ta.user_id
        JOIN stocks s ON s.id = ta.stock_id
        ORDER BY ta.id DESC
        """)
    List<TradeApplication> findAll();

    @Select("""
        SELECT *
        FROM trade_applications
        WHERE id = #{id}
        """)
    TradeApplication findById(Long id);

    @Insert("""
        INSERT INTO trade_applications
        (user_id, stock_id, type, quantity, price, amount, status)
        VALUES
        (#{userId}, #{stockId}, #{type}, #{quantity},
         #{price}, #{amount}, 'PENDING')
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TradeApplication application);

    @Update("""
        UPDATE trade_applications
        SET status = #{status},
            reason = #{reason},
            reviewed_at = NOW()
        WHERE id = #{id}
          AND status = 'PENDING'
        """)
    int review(@Param("id") Long id,
               @Param("status") String status,
               @Param("reason") String reason);
}