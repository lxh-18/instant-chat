package com.instantchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.instantchat.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
}