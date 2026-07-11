import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    CLEAR_AUTH(state) {
      state.token = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', user)
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  },
  getters: {
    isLoggedIn: state => !!state.token,
    username: state => state.userInfo.username || '',
    realName: state => state.userInfo.realName || '',
    roleCode: state => state.userInfo.roleCode || '',
    userId: state => state.userInfo.id || null,
    avatar: state => state.userInfo.avatar || ''
  }
})
