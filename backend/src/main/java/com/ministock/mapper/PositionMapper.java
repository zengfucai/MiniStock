package com.ministock.mapper;

import com.ministock.entity.Position;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PositionMapper {

    @Select("""
        SELECT p.*,
               s.code AS stock_code,
               s.name AS stock_name,
               s.price AS stock_price
        FROM positions p
        JOIN stocks s ON s.id = p.stock_id
        WHERE p.user_id = #{userId}
        ORDER BY p.id DESC
        """)
    List<Position> findByUserId(Long userId);

    @Select("""
        SELECT *
        FROM positions
        WHERE user_id = #{userId}
          AND stock_id = #{stockId}
        """)
    Position findByUserAndStock(@Param("userId") Long userId,
                                @Param("stockId") Long stockId);

    @Insert("""
        INSERT INTO positions
        (user_id, stock_id, quantity, average_price)
        VALUES
        (#{userId}, #{stockId}, #{quantity}, #{averagePrice})
        """)
    int insert(Position position);

    @Update("""
        UPDATE positions
        SET quantity = #{quantity},
            average_price = #{averagePrice}
        WHERE id = #{id}
        """)
    int update(Position position);

    @Delete("""
        DELETE FROM positions
        WHERE id = #{id}
        """)
    int delete(Long id);
}