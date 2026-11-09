export interface User {
    id: number;
    userName: string;
    hashedPassword: string;
    isActive: boolean;
    createTime: Date;
}
