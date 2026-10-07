package com.ministock.mapper;

import com.ministock.entity.Stock;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StockMapper {

    @Select("""
        SELECT *
        FROM stocks
        ORDER BY id DESC
        """)
    List<Stock> findAll();

    @Select("""
        SELECT *
        FROM stocks
        WHERE id = #{id}
        """)
    Stock findById(Long id);

    @Insert("""
        INSERT INTO stocks(code, name, price, change_rate, status)
        VALUES(#{code}, #{name}, #{price}, #{changeRate}, #{status})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Stock stock);

    @Update("""
        UPDATE stocks
        SET code = #{code},
            name = #{name},
            price = #{price},
            change_rate = #{changeRate},
            status = #{status}
        WHERE id = #{id}
        """)
    int update(Stock stock);

    @Delete("""
        DELETE FROM stocks
        WHERE id = #{id}
        """)
    int delete(Long id);
}