import request from '@/utils/request'

export function getBookPage(params) {
  return request.get('/books/page', { params })
}

export function getBookById(id) {
  return request.get(`/books/${id}`)
}

export function addBook(data) {
  return request.post('/books', data)
}

export function updateBook(data) {
  return request.put('/books', data)
}

export function deleteBook(id) {
  return request.delete(`/books/${id}`)
}

export function getHotBooks() {
  return request.get('/books/hot')
}

export function refreshHotBooks() {
  return request.post('/books/hot/refresh')
}
