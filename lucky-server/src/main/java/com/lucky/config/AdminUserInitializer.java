package com.lucky.config;

import com.lucky.entity.AdminUser;
import com.lucky.mapper.AdminUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化管理员用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // 检查是否已有管理员用户
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, "admin")
        );

        if (count == null || count == 0) {
            // 创建默认管理员
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole("SUPER_ADMIN");
            adminUserMapper.insert(adminUser);
            log.info("默认管理员用户已创建: admin/admin123");
        } else {
            log.info("管理员用户已存在，跳过初始化");
        }
    }
}
