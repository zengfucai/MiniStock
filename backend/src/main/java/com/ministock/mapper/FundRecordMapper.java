package com.ministock.mapper;

import com.ministock.entity.FundRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FundRecordMapper {

    @Insert("""
        INSERT INTO fund_records
        (user_id, type, amount, balance_after, description)
        VALUES
        (#{userId}, #{type}, #{amount},
         #{balanceAfter}, #{description})
        """)
    int insert(FundRecord record);

    @Select("""
        SELECT fr.*,
               u.username
        FROM fund_records fr
        JOIN users u ON u.id = fr.user_id
        WHERE fr.user_id = #{userId}
        ORDER BY fr.id DESC
        """)
    List<FundRecord> findByUserId(Long userId);

    @Select("""
        SELECT fr.*,
               u.username
        FROM fund_records fr
        JOIN users u ON u.id = fr.user_id
        ORDER BY fr.id DESC
        """)
    List<FundRecord> findAll();
}