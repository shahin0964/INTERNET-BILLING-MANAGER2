import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const settingsRouter = Router();

// GET ISP Settings
settingsRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const resSettings = await query('SELECT * FROM isp_settings WHERE id = 1');
        if (resSettings.rows.length === 0) {
            const defaultInsert = await query(`
                INSERT INTO isp_settings (id, company_name, tagline, currency_symbol)
                VALUES (1, 'Internet Service Provider', 'Fast • Stable • Connected', '৳')
                RETURNING *
            `);
            return res.json({ success: true, data: defaultInsert.rows[0] });
        }
        res.json({ success: true, data: resSettings.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// PUT update ISP Settings
settingsRouter.put('/', authenticate, async (req: Request, res: Response) => {
    try {
        const {
            companyName,
            tagline,
            phone,
            email,
            address,
            currencySymbol = '৳',
            networkStatus = 'Operational',
            logoUri = ''
        } = req.body;

        const updateRes = await query(
            `UPDATE isp_settings SET
                company_name = COALESCE($1, company_name),
                tagline = COALESCE($2, tagline),
                phone = COALESCE($3, phone),
                email = COALESCE($4, email),
                address = COALESCE($5, address),
                currency_symbol = COALESCE($6, currency_symbol),
                network_status = COALESCE($7, network_status),
                logo_uri = COALESCE($8, logo_uri),
                updated_at = CURRENT_TIMESTAMP
             WHERE id = 1 RETURNING *`,
            [companyName, tagline, phone, email, address, currencySymbol, networkStatus, logoUri]
        );

        res.json({ success: true, data: updateRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
