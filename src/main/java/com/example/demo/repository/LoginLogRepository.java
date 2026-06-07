package com.example.demo.repository;

import com.example.demo.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 登录日志数据访问接口。
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    /**
     * 根据用户ID查询最近登录记录，按时间倒序。
     */
    List<LoginLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 统计指定账号在最近一段时间内的失败次数。
     * 建议配合 created_at 时间条件使用。
     */
    long countByLoginAccountAndLoginResultAndCreatedAtAfter(String loginAccount, Integer loginResult, java.time.LocalDateTime since);
}
