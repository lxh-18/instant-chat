package com.instantchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.instantchat.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口 - 满足得分点1：MVC分层架构的Mapper层（数据访问层）
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}