package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ResultCode;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.mapper.RoleMapper;
import com.library.mapper.UserMapper;
import com.library.security.JwtUtils;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.key.user-token}")
    private String userTokenKeyPrefix;

    private static final int MAX_BORROW_LIMIT = 5;

    @Override
    public Map<String, Object> login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            // Store token in Redis for session management
            redisTemplate.opsForValue().set(
                    userTokenKeyPrefix + username, token,
                    jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);

            User user = userMapper.selectByUsername(username);
            user.setPassword(null); // Don't expose password

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            return result;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
    }

    @Override
    @Transactional
    public User register(User user) {
        // Validate
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名和密码不能为空");
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // Set reader role
        Role readerRole = roleMapper.selectList(null).stream()
                .filter(r -> "READER".equals(r.getRoleCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("读者角色未配置"));

        user.setRoleId(readerRole.getId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setReaderNo(generateReaderNo());
        user.setGender(user.getGender() == null ? 0 : user.getGender());

        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public Map<String, Object> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = userMapper.selectUserPage(page, keyword);

        // Mask passwords
        result.getRecords().forEach(u -> u.setPassword(null));

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("pageNum", result.getCurrent());
        map.put("pageSize", result.getSize());
        map.put("records", result.getRecords());
        return map;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User addUser(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        if (StrUtil.isBlank(user.getReaderNo()) && user.getRoleId() != null) {
            user.setReaderNo(generateReaderNo());
        }
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existing = userMapper.selectUserById(user.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // Don't update password here
        user.setPassword(null);
        // If username changed, check uniqueness
        if (StrUtil.isNotBlank(user.getUsername()) && !user.getUsername().equals(existing.getUsername())) {
            if (userMapper.selectByUsername(user.getUsername()) != null) {
                throw new BusinessException(ResultCode.USER_EXISTS);
            }
        }
        userMapper.updateById(user);
        return userMapper.selectUserById(user.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(id);
        // Clear Redis token
        redisTemplate.delete(userTokenKeyPrefix + user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        User update = new User();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
    }

    @Override
    public List<User> getAllReaders() {
        List<User> readers = userMapper.selectAllReaders();
        readers.forEach(u -> u.setPassword(null));
        return readers;
    }

    @Override
    public User getCurrentUser(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    private String generateReaderNo() {
        return "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
