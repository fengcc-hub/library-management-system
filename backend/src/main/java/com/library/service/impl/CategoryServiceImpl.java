package com.library.service.impl;

import com.library.entity.Category;
import com.library.exception.BusinessException;
import com.library.mapper.CategoryMapper;
import com.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectAllWithBookCount();
        // Build tree: filter root categories (parent_id = 0)
        List<Category> roots = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        // Recursively set children
        roots.forEach(root -> buildTree(root, allCategories));
        return roots;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAllWithBookCount();
    }

    @Override
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectCategoryById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    @Override
    @Transactional
    public Category addCategory(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
        return categoryMapper.selectCategoryById(category.getId());
    }

    @Override
    @Transactional
    public Category updateCategory(Category category) {
        Category existing = categoryMapper.selectCategoryById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.updateById(category);
        return categoryMapper.selectCategoryById(category.getId());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectCategoryById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        // Check if category has books
        if (category.getBookCount() != null && category.getBookCount() > 0) {
            throw new BusinessException("该分类下仍有图书，无法删除");
        }
        // Check children
        List<Category> children = categoryMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private void buildTree(Category parent, List<Category> all) {
        List<Category> children = all.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildTree(child, all));
    }
}
