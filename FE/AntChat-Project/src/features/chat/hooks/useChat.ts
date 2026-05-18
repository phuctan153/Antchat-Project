import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import {
    type ChatMessageResponse,
    type ChatMessageRequest,
    MessageType,
    type TypingEventPayload,
    type ReadReceiptPayload
} from '../../../types/chat.types';

// Dùng giao thức ws:// (hoặc wss:// nếu có https) thay vì http://
const SOCKET_URL = 'ws://localhost:8080/ws';

export const useChat = (roomId: number | null, token: string | null) => {
    const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
    const [typingUsers, setTypingUsers] = useState<string[]>([]);
    const [isConnected, setIsConnected] = useState<boolean>(false);

    // Sử dụng useRef để quản lý vòng đời client chặt chẽ trong React StrictMode
    const clientRef = useRef<Client | null>(null);
    const pingIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    useEffect(() => {
        if (!roomId || !token) return;

        // Khởi tạo STOMP Client thuần túy (không dùng SockJS)
        const client = new Client({
            brokerURL: SOCKET_URL,
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            reconnectDelay: 5000,
            debug: (str) => {
                // Tắt console log ở môi trường production
                if (import.meta.env.DEV) console.log('[STOMP]:', str);
            },

            onConnect: () => {
                setIsConnected(true);

                // 1. Lắng nghe tin nhắn
                client.subscribe(`/topic/room/${roomId}`, (message: IMessage) => {
                    const newMsg: ChatMessageResponse = JSON.parse(message.body);
                    setMessages((prev) => [...prev, newMsg]);
                });

                // 2. Lắng nghe sự kiện typing
                client.subscribe(`/topic/room/${roomId}/typing`, (message: IMessage) => {
                    const typingEvent: TypingEventPayload = JSON.parse(message.body);
                    setTypingUsers((prev) => {
                        if (typingEvent.isTyping) {
                            return prev.includes(typingEvent.username)
                                ? prev
                                : [...prev, typingEvent.username];
                        }
                        return prev.filter((name) => name !== typingEvent.username);
                    });
                });

                // 3. Lắng nghe sự kiện đã xem
                client.subscribe(`/topic/room/${roomId}/read`, (message: IMessage) => {
                    // eslint-disable-next-line @typescript-eslint/no-unused-vars
                    const readEvent: ReadReceiptPayload = JSON.parse(message.body);
                    setMessages((prev) => prev.map(msg => ({ ...msg, status: 'READ' })));
                });

                // 4. Gửi Heartbeat mỗi 3 phút theo yêu cầu Backend
                pingIntervalRef.current = setInterval(() => {
                    if (client.connected) {
                        client.publish({ destination: '/app/chat.ping', body: '' });
                    }
                }, 180000); // 180s
            },

            onDisconnect: () => {
                setIsConnected(false);
                if (pingIntervalRef.current) {
                    clearInterval(pingIntervalRef.current);
                }
            },

            onStompError: (frame) => {
                console.error('Lỗi Broker:', frame.headers['message']);
                console.error('Chi tiết:', frame.body);
            }
        });

        // Kích hoạt kết nối
        client.activate();
        clientRef.current = client;

        // Dọn dẹp (Cleanup) khi component unmount
        return () => {
            if (pingIntervalRef.current) {
                clearInterval(pingIntervalRef.current);
            }
            if (clientRef.current) {
                clientRef.current.deactivate();
            }
        };
    }, [roomId, token]); // Chạy lại hiệu ứng nếu roomId hoặc token thay đổi

    // Các hàm tương tác
    const sendMessage = useCallback((content: string, type: MessageType = MessageType.TEXT) => {
        if (clientRef.current?.connected && roomId) {
            const payload: ChatMessageRequest = { roomId, content, type };
            clientRef.current.publish({
                destination: '/app/chat.sendMessage',
                body: JSON.stringify(payload)
            });
        }
    }, [roomId]);

    const sendTyping = useCallback(() => {
        if (clientRef.current?.connected && roomId) {
            clientRef.current.publish({
                destination: '/app/chat.typing',
                body: roomId.toString()
            });
        }
    }, [roomId]);

    const loadMessageHistory = useCallback((historyMessages: ChatMessageResponse[]) => {
        setMessages(historyMessages);
    }, []);

    return {
        messages,
        typingUsers,
        isConnected,
        sendMessage,
        sendTyping,
        loadMessageHistory
    };
};