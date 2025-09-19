package com.devin.sso.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devin.sso.entity.User;
import com.devin.sso.mapper.UserMapper;
import com.devin.sso.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * (User)表服务实现类.
 *
 * @author joey
 */
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
}

