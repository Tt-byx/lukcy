package com.lucky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lucky.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员用户Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
