export const MessageType = {
    TEXT: 'TEXT',
    IMAGE: 'IMAGE',
    FILE: 'FILE',
    VIDEO: 'VIDEO',
    AUDIO: 'AUDIO'
} as const;

export type MessageType = (typeof MessageType)[keyof typeof MessageType];

export interface ChatMessageRequest {
    roomId: number;
    content: string;
    type: MessageType;
}

export interface ChatMessageResponse {
    id: number;
    roomId: number;
    senderId: number;
    senderName: string;
    content: string;
    type: MessageType;
    status: 'SENT' | 'DELIVERED' | 'READ';
    createdAt: string;
}

export interface TypingEventPayload {
    roomId: number;
    username: string;
    isTyping: boolean;
}

export interface ReadReceiptPayload {
    roomId: number;
    userId: number;
    username: string;
}