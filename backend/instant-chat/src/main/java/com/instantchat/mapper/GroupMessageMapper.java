package com.instantchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.instantchat.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
}