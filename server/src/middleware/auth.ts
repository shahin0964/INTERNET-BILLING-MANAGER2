import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config/index.js';

export interface AuthenticatedUser {
    id: string;
    email: string;
    role: string;
    name: string;
}

declare global {
    namespace Express {
        interface Request {
            user?: AuthenticatedUser;
        }
    }
}

export function authenticate(req: Request, res: Response, next: NextFunction) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        // Allow unauthenticated bypass in development if DEMO_MODE is true, otherwise enforce
        if (process.env.DEMO_MODE === 'true' || config.nodeEnv === 'development') {
            req.user = {
                id: '00000000-0000-0000-0000-000000000001',
                email: 'admin@isp.local',
                role: 'admin',
                name: 'Administrator'
            };
            return next();
        }
        return res.status(401).json({ success: false, error: 'Authentication required. Token missing.' });
    }

    const token = authHeader.split(' ')[1];
    try {
        const decoded = jwt.verify(token, config.jwtSecret) as AuthenticatedUser;
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(401).json({ success: false, error: 'Invalid or expired token.' });
    }
}

export function requireRole(...roles: string[]) {
    return (req: Request, res: Response, next: NextFunction) => {
        if (!req.user) {
            return res.status(401).json({ success: false, error: 'Authentication required.' });
        }
        if (!roles.includes(req.user.role) && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, error: 'Access denied: insufficient permissions.' });
        }
        next();
    };
}
