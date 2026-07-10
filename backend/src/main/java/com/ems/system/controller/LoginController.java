package com.ems.system.controller;

import com.ems.common.constant.SecurityConstants;
import com.ems.common.exception.BadRequestException;
import com.ems.common.utils.JwtUtil;
import com.ems.common.utils.ResultUtil;
import com.ems.common.utils.StringUtil;
import com.ems.config.security.JwtUser;
import com.ems.system.entity.SysUser;
import com.ems.system.entity.dto.UserDto;
import com.ems.system.service.SysUserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController extends ResultUtil {

    private final SysUserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    /** 用户登录（支持邮箱或用户名） */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserDto userDto) {
        try {
            String email = userDto.getUsername(); // username字段实际传的是email
            SysUser user = userService.findByEmail(email);
            if (user == null) {
                return fail("邮箱或密码错误");
            }
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = JwtUtil.generateToken(user.getId().toString(), user.getUsername(),
                    Collections.emptyList(), false);
            String refreshToken = JwtUtil.getRefreshToken(user.getId().toString(), user.getUsername());

            userDto.setId(user.getId());
            userDto.setNickName(user.getNickName());
            userDto.setPassword("******");

            return success(new JwtUser(token, refreshToken, userDto));
        } catch (BadRequestException e) {
            return fail(e.getMsg());
        }
    }

    /** 用户注册 */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserDto userDto) {
        try {
            // 检查用户名是否已存在
            SysUser exist = userService.findByName(userDto.getUsername());
            if (exist != null) {
                return fail("用户名已存在");
            }
            userService.editUser(userDto);
            return success("注册成功");
        } catch (BadRequestException e) {
            return fail(e.getMsg());
        }
    }

    /** 刷新 Token */
    @PutMapping("/refresh")
    public ResponseEntity<Object> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = request.getHeader(SecurityConstants.TOKEN_HEADER);
            if (StringUtil.isNotBlank(refreshToken)) {
                refreshToken = refreshToken.replaceFirst(SecurityConstants.TOKEN_PREFIX, "");
                Claims claims = JwtUtil.getRefreshTokenBody(refreshToken);
                if (claims.get("exp", Long.class) > 0) {
                    SysUser user = userService.findByName(claims.getSubject());
                    String token = JwtUtil.generateToken(user.getId().toString(), user.getUsername(),
                            Collections.emptyList(), false);
                    return success(token);
                }
            }
        } catch (BadRequestException e) {
            return fail(e.getMsg());
        }
        return fail("请重新登录");
    }
}
