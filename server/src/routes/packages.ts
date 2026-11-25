import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const packagesRouter = Router();

packagesRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const result = await query('SELECT * FROM packages ORDER BY speed_mbps ASC');
        res.json({ success: true, count: result.rows.length, data: result.rows });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

packagesRouter.post('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { name, speedMbps, monthlyPrice, description = 'High-speed optical fiber connection', colorHex = '#00E5FF' } = req.body;
        if (!name || speedMbps === undefined || monthlyPrice === undefined) {
            return res.status(400).json({ success: false, error: 'Name, speed, and monthly price are required.' });
        }

        const insertRes = await query(
            `INSERT INTO packages (name, speed_mbps, monthly_price, description, color_hex)
             VALUES ($1, $2, $3, $4, $5) RETURNING *`,
            [name, speedMbps, monthlyPrice, description, colorHex]
        );

        res.status(201).json({ success: true, data: insertRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

packagesRouter.put('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const { name, speedMbps, monthlyPrice, description, colorHex } = req.body;

        const updateRes = await query(
            `UPDATE packages SET
                name = COALESCE($1, name),
                speed_mbps = COALESCE($2, speed_mbps),
                monthly_price = COALESCE($3, monthly_price),
                description = COALESCE($4, description),
                color_hex = COALESCE($5, color_hex),
                updated_at = CURRENT_TIMESTAMP
             WHERE id = $6 RETURNING *`,
            [name, speedMbps, monthlyPrice, description, colorHex, id]
        );

        if (updateRes.rows.length === 0) {
            return res.status(404).json({ success: false, error: 'Package not found' });
        }

        res.json({ success: true, data: updateRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

packagesRouter.delete('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        await query('DELETE FROM packages WHERE id = $1', [id]);
        res.json({ success: true, message: 'Package deleted successfully' });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
