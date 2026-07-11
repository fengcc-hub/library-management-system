/**
 * 读书名言 + 随机图片数据
 * 每次页面加载随机展示不同内容
 */

// 读书名言列表
const quotes = [
  {
    text: '书山有路勤为径，学海无涯苦作舟',
    author: '韩愈'
  },
  {
    text: '读书破万卷，下笔如有神',
    author: '杜甫'
  },
  {
    text: '黑发不知勤学早，白首方悔读书迟',
    author: '颜真卿'
  },
  {
    text: '腹有诗书气自华',
    author: '苏轼'
  },
  {
    text: '书籍是人类进步的阶梯',
    author: '高尔基'
  },
  {
    text: '读万卷书，行万里路',
    author: '刘彝'
  },
  {
    text: '立身以立学为先，立学以读书为本',
    author: '欧阳修'
  },
  {
    text: '鸟欲高飞先振翅，人求上进先读书',
    author: '李苦禅'
  }
]

// 图片列表（require 确保 webpack 正确处理）
const images = [
  require('@/assets/images/reading.jpg'),
  require('@/assets/images/library.jpg'),
  require('@/assets/images/newspaper.jpg'),
  require('@/assets/images/leisure.jpg')
]

/**
 * 获取随机名言
 * @returns {Object} { text, author }
 */
export function getRandomQuote() {
  return quotes[Math.floor(Math.random() * quotes.length)]
}

/**
 * 获取随机图片
 * @returns {String} 图片路径
 */
export function getRandomImage() {
  return images[Math.floor(Math.random() * images.length)]
}

/**
 * 获取随机名言+图片组合
 * @returns {Object} { quote, image }
 */
export function getRandomContent() {
  return {
    quote: getRandomQuote(),
    image: getRandomImage()
  }
}

/**
 * 获取多条随机名言（不重复）
 * @param {Number} count 数量
 * @returns {Array} 名言数组
 */
export function getRandomQuotes(count = 1) {
  const shuffled = [...quotes].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, Math.min(count, quotes.length))
}

export { quotes, images }
