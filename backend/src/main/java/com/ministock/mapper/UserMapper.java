package com.ministock.mapper;

import com.ministock.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
        SELECT *
        FROM users
        WHERE username = #{username}
        LIMIT 1
        """)
    User findByUsername(String username);

    @Select("""
        SELECT *
        FROM users
        WHERE id = #{id}
        """)
    User findById(Long id);

    @Select("""
        SELECT *
        FROM users
        ORDER BY id DESC
        """)
    List<User> findAll();

    @Insert("""
        INSERT INTO users(username, password, role, status)
        VALUES(#{username}, #{password}, 'USER', 'NORMAL')
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
        UPDATE users
        SET status = #{status}
        WHERE id = #{id}
        """)
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status);
}