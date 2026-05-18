import axios from 'axios';
import { type ChatMessageResponse } from '../types/chat.types';

// Cấu hình URL mặc định cho Backend
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Tạo một instance axios với cấu hình sẵn (tự động đính kèm Token)
export const apiClient = axios.create({
    baseURL: API_URL,
});

// Interceptor: Tự động nhét JWT Token vào mọi request gửi đi
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('jwt_token'); // Hoặc lấy từ Zustand/Redux
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const ChatAPI = {
    // 1. Lấy lịch sử tin nhắn
    getMessages: async (roomId: number, page: number = 0, size: number = 20) => {
        const response = await apiClient.get<ChatMessageResponse[]>(`/messages/${roomId}`, {
            params: { page, size }
        });
        return response.data;
    },

    // 2. Lấy số tin nhắn chưa đọc
    getUnreadCount: async (roomId: number) => {
        const response = await apiClient.get<number>(`/messages/rooms/${roomId}/unread-count`);
        return response.data;
    },

    // 3. Đánh dấu đã đọc
    markRoomAsRead: async (roomId: number) => {
        const response = await apiClient.put<string>(`/messages/rooms/${roomId}/read`);
        return response.data;
    },

    // 4. Lấy trạng thái Online/Offline của user khác
    getUserStatus: async (userId: number) => {
        const response = await apiClient.get<string>(`/users/${userId}/status`);
        return response.data;
    },

    // 5. Upload Ảnh/File lên Cloudinary
    uploadMedia: async (file: File) => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiClient.post<string>('/media/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        return response.data; // Trả về Secure URL
    }
};