package com.mogo.project.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mogo.project.modules.system.model.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 用户身份封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private SysUser sysUser;

    private String token;

    private Long loginTime;//登录时间

    private Long expireTime;//失效时间

    private String browser;//浏览器

    private String os;//操作系统

    private String ipaddr; //IP地址


    // 权限列表
    private List<String> permissions;

    // 缓存转换后的权限对象
    @JsonIgnore
    private List<SimpleGrantedAuthority> authorities;

    public LoginUser(SysUser sysUser, List<String> permissions) {
        this.sysUser = sysUser;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        // 将字符串权限 (e.g. "user:list") 转换为 Security 需要的 Authority 对象
        if (permissions != null) {
            authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    // 账户是否未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户是否未锁定
    @Override
    public boolean isAccountNonLocked() {
        return sysUser.getStatus() != null && sysUser.getStatus() == 1;
    }

    // 凭证是否未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否可用
    @Override
    public boolean isEnabled() {
        return sysUser.getDeleteFlag()==0; // 假设 0 为未删除
    }
}