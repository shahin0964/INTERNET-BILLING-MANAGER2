import dotenv from 'dotenv';
dotenv.config();

export const config = {
    port: parseInt(process.env.PORT || '5000', 10),
    nodeEnv: process.env.NODE_ENV || 'development',
    databaseUrl: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/ibm_db',
    jwtSecret: process.env.JWT_SECRET || 'ibm_super_secret_jwt_key_2026',
    jwtExpiresIn: process.env.JWT_EXPIRES_IN || '7d',
    corsOrigin: process.env.CORS_ORIGIN || '*',
    mikrotik: {
        host: process.env.MIKROTIK_HOST || '192.168.88.1',
        port: parseInt(process.env.MIKROTIK_PORT || '8728', 10),
        user: process.env.MIKROTIK_USER || 'admin',
        pass: process.env.MIKROTIK_PASS || ''
    }
};
