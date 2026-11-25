import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { MikroTikService } from '../services/mikrotikService.js';
import { authenticate } from '../middleware/auth.js';

export const mikrotikRouter = Router();

// GET test ping / connectivity to MikroTik router
mikrotikRouter.get('/status', authenticate, async (req: Request, res: Response) => {
    try {
        const { host, port } = req.query;
        const testResult = await MikroTikService.testConnection(
            host as string,
            port ? parseInt(port as string, 10) : undefined
        );
        res.json({ success: true, data: testResult });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// GET MikroTik configuration
mikrotikRouter.get('/config', authenticate, async (req: Request, res: Response) => {
    try {
        const config = await MikroTikService.getConfig();
        // Hide password in response
        const safeConfig = { ...config, password: config.password ? '******' : '' };
        res.json({ success: true, data: safeConfig });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// PUT update MikroTik configuration
mikrotikRouter.put('/config', authenticate, async (req: Request, res: Response) => {
    try {
        const { host, port = 8728, username = 'admin', password, isEnabled = true } = req.body;
        if (!host) {
            return res.status(400).json({ success: false, error: 'Host is required' });
        }

        let updateSql = `
            UPDATE mikrotik_configs SET
                host = $1,
                port = $2,
                username = $3,
                is_enabled = $4,
                updated_at = CURRENT_TIMESTAMP
        `;
        const params: any[] = [host, port, username, isEnabled];

        if (password && password !== '******') {
            params.push(password);
            updateSql += `, password = $${params.length}`;
        }

        updateSql += ` WHERE id = 1 RETURNING id, host, port, username, is_enabled, updated_at`;

        const updateRes = await query(updateSql, params);
        res.json({ success: true, data: updateRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST kick / disconnect user
mikrotikRouter.post('/kick-user', authenticate, async (req: Request, res: Response) => {
    try {
        const { username } = req.body;
        if (!username) {
            return res.status(400).json({ success: false, error: 'PPPoE Username is required.' });
        }

        const result = await MikroTikService.kickUser(username);
        res.json({ success: true, data: result });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
