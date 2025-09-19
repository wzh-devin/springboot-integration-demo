package com.devin.sso.mapper;

import com.devin.sso.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * (User)表数据库访问层.
 *
 * @author joey
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
    
