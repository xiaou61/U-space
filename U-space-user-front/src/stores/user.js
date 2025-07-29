import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: '',
    studentNo: '',
    name: '',
    classId: '',
    phone: '',
  }),
  actions: {
    setUserInfo(info) {
      Object.assign(this, info)
    },
  },
}) 