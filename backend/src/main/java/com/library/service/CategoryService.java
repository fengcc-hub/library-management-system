package com.library.service;

import com.library.entity.Category;

import java.util.List;

public interface CategoryService {

    /** Get all categories as tree */
    List<Category> getCategoryTree();

    /** Get all categories flat list */
    List<Category> getAllCategories();

    /** Get category by id */
    Category getCategoryById(Long id);

    /** Add category */
    Category addCategory(Category category);

    /** Update category */
    Category updateCategory(Category category);

    /** Delete category */
    void deleteCategory(Long id);
}
