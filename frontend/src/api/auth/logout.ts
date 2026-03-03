import axios from 'axios'

const logoutClient = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

export async function logoutApi(accessToken?: string) {
  try {
    await logoutClient.post(
      '/auth/logout',
      {},
      { headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {} }
    )
  } catch {
    // ignore
  }
}