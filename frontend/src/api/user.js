import request from '@/utils/request'

export function getUserPage(params) {
  return request.get('/users/page', { params })
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}

export function addUser(data) {
  return request.post('/users', data)
}

export function updateUser(data) {
  return request.put('/users', data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function resetPassword(id, password) {
  return request.put(`/users/${id}/password`, { password })
}

export function getAllReaders() {
  return request.get('/users/readers')
}
