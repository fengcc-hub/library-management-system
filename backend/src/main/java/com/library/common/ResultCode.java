package com.library.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result Code Enum
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_EXISTS(1004, "用户名已存在"),

    BOOK_NOT_FOUND(2001, "图书不存在"),
    BOOK_OUT_OF_STOCK(2002, "图书库存不足"),
    BOOK_OFF_SHELF(2003, "图书已下架"),

    BORROW_LIMIT_EXCEEDED(3001, "借阅数量已达上限"),
    BORROW_RECORD_NOT_FOUND(3002, "借阅记录不存在"),
    BORROW_ALREADY_RETURNED(3003, "该图书已归还"),
    BORROW_OVERDUE(3004, "存在逾期未还图书，暂不可借"),
    RENEW_LIMIT_EXCEEDED(3005, "续借次数已达上限"),

    FINE_UNPAID(4001, "存在未缴罚款，暂不可借");

    private final Integer code;
    private final String message;
}
