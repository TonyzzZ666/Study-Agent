package com.ems.config.security;

import com.ems.common.exception.BadRequestException;
import com.ems.system.entity.SysUser;
import com.ems.system.entity.dto.JwtUserDto;
import com.ems.system.entity.dto.UserDto;
import com.ems.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserDto user = null;
        try {
            SysUser sysUser = userService.findByEmail(loginId);
            if (sysUser != null) {
                user = userService.loadByName(sysUser.getUsername());
            }
        } catch (BadRequestException e) {
            throw new UsernameNotFoundException("邮箱或密码错误", e);
        }
        if (user == null) {
            throw new UsernameNotFoundException("邮箱或密码错误");
        }
        if (!user.getEnabled()) {
            throw new BadRequestException("账号未激活！");
        }
        return new JwtUserDto(user, Collections.emptyList());
    }
}
