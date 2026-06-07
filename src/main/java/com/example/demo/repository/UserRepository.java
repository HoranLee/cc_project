package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户。
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户。
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户（用于登录）。
     */
    @Query("SELECT u FROM User u WHERE u.username = :account OR u.email = :account")
    Optional<User> findByUsernameOrEmail(@Param("account") String account);

    /**
     * 检查用户名是否已存在。
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在。
     */
    boolean existsByEmail(String email);
}
