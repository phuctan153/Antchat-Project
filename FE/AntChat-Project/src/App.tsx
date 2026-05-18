import { useState } from 'react';
import { ChatRoom } from './features/chat/components/ChatRoom';

function App() {
  // Trạng thái lưu trữ user đăng nhập tạm thời để test
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [token, setToken] = useState('');
  const [currentUserId, setCurrentUserId] = useState<number | ''>('');

  // Trạng thái chọn phòng chat (Layout Zalo)
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);

  // Xử lý đăng nhập tạm
  const handleLogin = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (token && currentUserId) {
      localStorage.setItem('jwt_token', token);
      setIsLoggedIn(true);
    }
  };

  // Nếu chưa "đăng nhập", hiển thị màn hình nhập Token
  if (!isLoggedIn) {
    return (
        <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', backgroundColor: '#f0f2f5' }}>
          <form onSubmit={handleLogin} style={{ background: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', gap: '15px', width: '350px' }}>
            <h2 style={{ textAlign: 'center', margin: 0 }}>Vào AntChat</h2>
            <p style={{ fontSize: '12px', color: '#666', marginTop: 0 }}>Hãy lấy JWT Token từ Swagger/Postman dán vào đây để test.</p>

            <input
                type="number"
                placeholder="Nhập ID của bạn (VD: 1)"
                value={currentUserId}
                onChange={(e) => setCurrentUserId(Number(e.target.value))}
                style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
                required
            />
            <input
                type="text"
                placeholder="Nhập JWT Token..."
                value={token}
                onChange={(e) => setToken(e.target.value)}
                style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
                required
            />
            <button type="submit" style={{ padding: '10px', background: '#0084ff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
              Kết nối Backend
            </button>
          </form>
        </div>
    );
  }

  // GIAO DIỆN CHÍNH (LAYOUT ZALO)
  return (
      <div style={{ display: 'flex', height: '100vh', width: '100vw', overflow: 'hidden', fontFamily: 'sans-serif' }}>

        {/* CỘT TRÁI: SIDEBAR (Danh sách phòng) */}
        <div style={{ width: '300px', borderRight: '1px solid #ddd', background: '#fff', display: 'flex', flexDirection: 'column' }}>
          <div style={{ padding: '16px', borderBottom: '1px solid #ddd', background: '#f8f9fa' }}>
            <h2 style={{ margin: 0, fontSize: '20px' }}>Chat</h2>
            <small>ID của bạn: {currentUserId}</small>
          </div>

          <div style={{ flex: 1, overflowY: 'auto' }}>
            {/* MOCK DATA: Giả lập danh sách phòng (Sau này sẽ gọi API lấy danh sách thực tế) */}
            {[1, 2, 3].map((roomId) => (
                <div
                    key={roomId}
                    onClick={() => setSelectedRoomId(roomId)}
                    style={{
                      padding: '15px',
                      borderBottom: '1px solid #f0f0f0',
                      cursor: 'pointer',
                      background: selectedRoomId === roomId ? '#e6f2ff' : 'transparent',
                      transition: 'background 0.2s'
                    }}
                >
                  <strong>Phòng Chat #{roomId}</strong>
                  <div style={{ fontSize: '12px', color: '#888', marginTop: '4px' }}>Nhấn để vào trò chuyện</div>
                </div>
            ))}
          </div>
        </div>

        {/* CỘT PHẢI: KHUNG CHAT (Main Content) */}
        <div style={{ flex: 1, background: '#f0f2f5', display: 'flex', flexDirection: 'column' }}>
          {selectedRoomId ? (
              // Đã chọn phòng -> Hiển thị Component ChatRoom đã viết ở bước trước
              // Lưu ý: key={selectedRoomId} giúp React reset lại hook useChat khi chuyển phòng
              <ChatRoom key={selectedRoomId} roomId={selectedRoomId} currentUserId={Number(currentUserId)} />
          ) : (
              // Chưa chọn phòng -> Màn hình chờ
              <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', color: '#888' }}>
                <h2>Chào mừng đến với AntChat</h2>
                <p>Hãy chọn một phòng bên trái để bắt đầu nhắn tin</p>
              </div>
          )}
        </div>

      </div>
  );
}

export default App;