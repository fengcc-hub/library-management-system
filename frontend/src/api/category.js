import request from '@/utils/request'

export function getCategoryTree() {
  return request.get('/categories/tree')
}

export function getAllCategories() {
  return request.get('/categories')
}

export function addCategory(data) {
  return request.post('/categories', data)
}

export function updateCategory(data) {
  return request.put('/categories', data)
}

export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
