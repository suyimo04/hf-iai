import { defineStore } from 'pinia'
import type { User, Role } from '@/types'
import type { ApiResponse, LoginResponse } from '@/types/api'
import { getToken, setToken, removeToken } from '@/utils/auth'
import request from '@/utils/request'

interface UserState {
  token: string | null
  user: User | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    user: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role,
    hasPermission: (state) => {
      return (roles: Role[]) => {
        if (!state.user?.role) return false
        return roles.includes(state.user.role)
      }
    }
  },

  actions: {
    async login(username: string, password: string) {
      const response = await request.post<ApiResponse<LoginResponse>>('/auth/login', {
        username,
        password
      })
      const { token, user } = response.data.data
      this.token = token
      this.user = user
      setToken(token)
      return user
    },

    logout() {
      // JWT无状态，只需清除本地token
      this.token = null
      this.user = null
      removeToken()
    },

    async fetchUserInfo() {
      const response = await request.get<ApiResponse<User>>('/auth/me')
      this.user = response.data.data
      return this.user
    },

    setUser(user: User) {
      this.user = user
    }
  }
})
