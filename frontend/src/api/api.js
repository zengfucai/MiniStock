import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    withCredentials: true
})

api.interceptors.response.use(
    response => {

        const data = response.data

        if (
            data &&
            data.success === false
        ) {

            const message =
                data.message ||
                '请求失败'

            alert(message)

            return Promise.reject(
                new Error(message)
            )
        }

        return data
    },

    error => {

        const message =
            error.response?.data?.message ||
            error.response?.data?.error ||
            '请求失败'

        alert(message)

        return Promise.reject(error)
    }
)

export default api