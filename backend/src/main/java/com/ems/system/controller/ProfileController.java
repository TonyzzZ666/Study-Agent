package com.ems.system.controller;

import com.ems.common.utils.ResultUtil;
import com.ems.system.entity.SysUser;
import com.ems.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController extends ResultUtil {

    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) auth.getPrincipal()).getUsername());
    }

    /** 获取个人信息 */
    @GetMapping
    public ResponseEntity<Object> getProfile() {
        SysUser user = userService.getById(getCurrentUserId());
        if (user == null) return fail("用户不存在");
        user.setPassword(null); // 不返回密码
        return success(user);
    }

    /** 更新个人信息 */
    @PutMapping
    public ResponseEntity<Object> updateProfile(@RequestBody Map<String, Object> body) {
        SysUser user = userService.getById(getCurrentUserId());
        if (user == null) return fail("用户不存在");
        if (body.containsKey("nickName")) user.setNickName((String) body.get("nickName"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("signature")) user.setSignature((String) body.get("signature"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        userService.updateById(user);
        return success("更新成功");
    }

    /** 修改密码 */
    @PutMapping("/password")
    public ResponseEntity<Object> changePassword(@RequestBody Map<String, String> body) {
        SysUser user = userService.getById(getCurrentUserId());
        if (user == null) return fail("用户不存在");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) return fail("旧密码错误");
        user.setPassword(passwordEncoder.encode(newPwd));
        userService.updateById(user);
        return success("密码修改成功");
    }
}
