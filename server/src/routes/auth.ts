import { Router, Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { query } from '../db/index.js';
import { config } from '../config/index.js';
import { authenticate } from '../middleware/auth.js';

export const authRouter = Router();

authRouter.post('/login', async (req: Request, res: Response) => {
    try {
        const { email, password } = req.body;
        if (!email || !password) {
            return res.status(400).json({ success: false, error: 'Email and password are required.' });
        }

        const result = await query('SELECT * FROM users WHERE email = $1 LIMIT 1', [email.toLowerCase().trim()]);
        
        if (result.rows.length === 0) {
            // First time setup: If no users exist, automatically create default admin user
            const countRes = await query('SELECT COUNT(*) FROM users');
            if (parseInt(countRes.rows[0].count, 10) === 0) {
                const hash = await bcrypt.hash(password, 10);
                const newAdmin = await query(
                    `INSERT INTO users (name, email, password_hash, role) 
                     VALUES ($1, $2, $3, 'admin') RETURNING id, name, email, role`,
                    ['ISP Administrator', email.toLowerCase().trim(), hash]
                );
                const user = newAdmin.rows[0];
                const token = jwt.sign(
                    { id: user.id, email: user.email, role: user.role, name: user.name },
                    config.jwtSecret,
                    { expiresIn: '7d' }
                );
                return res.json({ success: true, token, user, message: 'Initial Admin account created!' });
            }
            return res.status(401).json({ success: false, error: 'Invalid email or password.' });
        }

        const user = result.rows[0];
        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch) {
            return res.status(401).json({ success: false, error: 'Invalid email or password.' });
        }

        const token = jwt.sign(
            { id: user.id, email: user.email, role: user.role, name: user.name },
            config.jwtSecret,
            { expiresIn: '7d' }
        );

        return res.json({
            success: true,
            token,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role
            }
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

authRouter.post('/register', async (req: Request, res: Response) => {
    try {
        const { name, email, password, role = 'operator' } = req.body;
        if (!name || !email || !password) {
            return res.status(400).json({ success: false, error: 'Name, email, and password are required.' });
        }

        const existing = await query('SELECT id FROM users WHERE email = $1', [email.toLowerCase().trim()]);
        if (existing.rows.length > 0) {
            return res.status(400).json({ success: false, error: 'Email already registered.' });
        }

        const hash = await bcrypt.hash(password, 10);
        const insertRes = await query(
            `INSERT INTO users (name, email, password_hash, role) 
             VALUES ($1, $2, $3, $4) RETURNING id, name, email, role`,
            [name.trim(), email.toLowerCase().trim(), hash, role]
        );
        const user = insertRes.rows[0];

        const token = jwt.sign(
            { id: user.id, email: user.email, role: user.role, name: user.name },
            config.jwtSecret,
            { expiresIn: '7d' }
        );

        res.status(201).json({ success: true, token, user });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

authRouter.get('/me', authenticate, async (req: Request, res: Response) => {
    try {
        const user = req.user;
        const settingsRes = await query('SELECT * FROM isp_settings WHERE id = 1');
        res.json({
            success: true,
            user,
            ispSettings: settingsRes.rows[0] || null
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
