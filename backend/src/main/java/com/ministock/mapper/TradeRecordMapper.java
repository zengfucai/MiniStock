package com.ministock.mapper;

import com.ministock.entity.TradeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeRecordMapper {

    @Insert("""
        INSERT INTO trade_records
        (user_id, stock_id, type, quantity, price, amount)
        VALUES
        (#{userId}, #{stockId}, #{type},
         #{quantity}, #{price}, #{amount})
        """)
    int insert(TradeRecord record);

    @Select("""
        SELECT tr.*,
               u.username,
               s.code AS stock_code,
               s.name AS stock_name
        FROM trade_records tr
        JOIN users u ON u.id = tr.user_id
        JOIN stocks s ON s.id = tr.stock_id
        WHERE tr.user_id = #{userId}
        ORDER BY tr.id DESC
        """)
    List<TradeRecord> findByUserId(Long userId);

    @Select("""
        SELECT tr.*,
               u.username,
               s.code AS stock_code,
               s.name AS stock_name
        FROM trade_records tr
        JOIN users u ON u.id = tr.user_id
        JOIN stocks s ON s.id = tr.stock_id
        ORDER BY tr.id DESC
        """)
    List<TradeRecord> findAll();
}