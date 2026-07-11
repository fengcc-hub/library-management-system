import request from '@/utils/request'

export function borrowBook(data) {
  return request.post('/borrow/borrow', data)
}

export function returnBook(recordId) {
  return request.put(`/borrow/return/${recordId}`)
}

export function renewBook(recordId) {
  return request.put(`/borrow/renew/${recordId}`)
}

export function payFine(recordId) {
  return request.put(`/borrow/pay-fine/${recordId}`)
}

export function getBorrowPage(params) {
  return request.get('/borrow/page', { params })
}

export function getRecordById(id) {
  return request.get(`/borrow/${id}`)
}

export function getActiveBorrows(userId) {
  return request.get(`/borrow/active/${userId}`)
}

export function checkOverdue() {
  return request.post('/borrow/check-overdue')
}
