package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String isbn;
    private String title;
    private String author;
    private String publisher;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    private Long categoryId;
    private java.math.BigDecimal price;
    private String location;
    private Integer totalCount;
    private Integer availableCount;
    private String description;
    private String coverUrl;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    /** Transient: category name */
    @TableField(exist = false)
    private String categoryName;

    /** Transient: borrow count for hot book stats */
    @TableField(exist = false)
    private Integer borrowCount;
}
