import {createStore} from 'vuex'

export default createStore({
    state: {
        user: null,
    },
    mutations: {
        setUser(state, user) {
            state.user = user
        },
        setToken(state, token) {
            localStorage.setItem('fm_token', token);
            state.token = token;
        },
        clearToken(state) {
            state.token = ''
            localStorage.removeItem('fm_token');
        },
    },
    actions: {
        login({commit}, user) {
            commit('setUser', user)
            commit('setToken', user.token)
            localStorage.setItem('isLoggedIn', 'true')
        },
        logout({commit}) {
            commit('setUser', null)
            commit('clearToken')
            localStorage.removeItem('isLoggedIn')
        }
    }
})
