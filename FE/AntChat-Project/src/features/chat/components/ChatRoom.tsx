import { useState, useEffect, useRef } from 'react';
import { useChat } from '../hooks/useChat';
import { ChatAPI } from '../../../api/chat.api';
import { MessageType } from '../../../types/chat.types';

interface ChatRoomProps {
    roomId: number;
    currentUserId: number; // ID của chính bạn để phân biệt tin nhắn gửi/nhận
}

export const ChatRoom = ({ roomId, currentUserId }: ChatRoomProps) => {
    // Lấy token từ nơi bạn lưu trữ (ví dụ: localStorage)
    const token = localStorage.getItem('jwt_token');

    // Khởi tạo Custom Hook đã viết ở Bước 3
    const { messages, typingUsers, isConnected, sendMessage, sendTyping, loadMessageHistory } = useChat(roomId, token);

    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    // 1. Tải lịch sử tin nhắn khi mới vào phòng
    useEffect(() => {
        const fetchHistory = async () => {
            try {
                setIsLoading(true);
                const history = await ChatAPI.getMessages(roomId, 0, 50);
                // Đảo ngược mảng nếu Backend trả về tin nhắn mới nhất ở đầu mảng
                loadMessageHistory(history.reverse());
            } catch (error) {
                console.error('Lỗi khi tải lịch sử:', error);
            } finally {
                setIsLoading(false);
            }
        };

        if (roomId) fetchHistory();
    }, [roomId, loadMessageHistory]);

    // 2. Tự động cuộn xuống cuối khi có tin nhắn mới
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // 3. Xử lý khi nhấn nút gửi
    const handleSendMessage = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!inputValue.trim()) return;

        sendMessage(inputValue, MessageType.TEXT);
        setInputValue('');
    };

    // 4. Xử lý khi người dùng đang gõ phím
    const handleTyping = (e: React.ChangeEvent<HTMLInputElement>) => {
        setInputValue(e.target.value);
        sendTyping(); // Bắn event lên WebSocket
    };

    if (isLoading) return <div>Đang tải tin nhắn...</div>;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', maxWidth: '600px', margin: '0 auto', border: '1px solid #ccc' }}>
            {/* Header */}
            <div style={{ padding: '16px', background: '#f0f2f5', borderBottom: '1px solid #ddd' }}>
                <h3>Phòng Chat #{roomId}</h3>
                <small style={{ color: isConnected ? 'green' : 'red' }}>
                    {isConnected ? '🟢 Đã kết nối' : '🔴 Mất kết nối'}
                </small>
            </div>

            {/* Khung hiển thị tin nhắn */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '16px', background: '#fff' }}>
                {messages.map((msg, index) => {
                    const isMine = msg.senderId === currentUserId;
                    return (
                        <div key={msg.id || index} style={{ display: 'flex', justifyContent: isMine ? 'flex-end' : 'flex-start', marginBottom: '12px' }}>
                            <div style={{
                                maxWidth: '70%',
                                padding: '10px 14px',
                                borderRadius: '18px',
                                background: isMine ? '#0084ff' : '#e4e6eb',
                                color: isMine ? '#fff' : '#000'
                            }}>
                                {!isMine && <small style={{ display: 'block', fontSize: '11px', color: '#666', marginBottom: '4px' }}>{msg.senderName}</small>}
                                <span>{msg.content}</span>
                                <div style={{ fontSize: '10px', textAlign: 'right', marginTop: '4px', opacity: 0.7 }}>
                                    {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </div>
                            </div>
                        </div>
                    );
                })}
                <div ref={messagesEndRef} /> {/* Dummy div để auto-scroll */}
            </div>

            {/* Hiển thị Typing indicator */}
            {typingUsers.length > 0 && (
                <div style={{ padding: '0 16px 8px', fontSize: '12px', color: '#666', fontStyle: 'italic' }}>
                    {typingUsers.join(', ')} đang gõ...
                </div>
            )}

            {/* Input gửi tin */}
            <form onSubmit={handleSendMessage} style={{ display: 'flex', padding: '16px', borderTop: '1px solid #ddd', background: '#f0f2f5' }}>
                <input
                    type="text"
                    value={inputValue}
                    onChange={handleTyping}
                    placeholder="Nhập tin nhắn..."
                    style={{ flex: 1, padding: '10px', borderRadius: '20px', border: '1px solid #ccc', outline: 'none' }}
                />
                <button type="submit" disabled={!isConnected || !inputValue.trim()} style={{ marginLeft: '12px', padding: '10px 20px', borderRadius: '20px', border: 'none', background: '#0084ff', color: '#fff', cursor: 'pointer' }}>
                    Gửi
                </button>
            </form>
        </div>
    );
};