import request from '@/utils/request'

export function getOverview() {
  return request.get('/statistics/overview')
}

export function getMonthlyTrend(months = 6) {
  return request.get('/statistics/borrow/monthly', { params: { months } })
}

export function getDailyTrend(days = 30) {
  return request.get('/statistics/borrow/daily', { params: { days } })
}

export function getCategoryDistribution() {
  return request.get('/statistics/category-distribution')
}

export function getTopBooks(limit = 10) {
  return request.get('/statistics/top-books', { params: { limit } })
}

export function getReaderRanking(limit = 10) {
  return request.get('/statistics/reader-ranking', { params: { limit } })
}
