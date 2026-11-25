import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { PaymentService } from '../services/paymentService.js';
import { authenticate } from '../middleware/auth.js';

export const paymentsRouter = Router();

// GET payments list with filters
paymentsRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { monthYear, customerId, today, yesterday, limit = 50 } = req.query;
        let sql = `SELECT * FROM payments`;
        const params: any[] = [];
        const conditions: string[] = [];

        if (monthYear && typeof monthYear === 'string') {
            params.push(monthYear);
            conditions.push(`month_year = $${params.length}`);
        }

        if (customerId) {
            params.push(customerId);
            conditions.push(`customer_id = $${params.length}`);
        }

        const now = new Date();
        if (today === 'true') {
            const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
            const endOfDay = startOfDay + 86400000 - 1;
            params.push(startOfDay, endOfDay);
            conditions.push(`date_millis >= $${params.length - 1} AND date_millis <= $${params.length}`);
        } else if (yesterday === 'true') {
            const yesterdayDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1);
            const startOfDay = yesterdayDate.getTime();
            const endOfDay = startOfDay + 86400000 - 1;
            params.push(startOfDay, endOfDay);
            conditions.push(`date_millis >= $${params.length - 1} AND date_millis <= $${params.length}`);
        }

        if (conditions.length > 0) {
            sql += ` WHERE ` + conditions.join(' AND ');
        }

        sql += ` ORDER BY date_millis DESC LIMIT $${params.length + 1}`;
        params.push(parseInt(limit as string, 10));

        const result = await query(sql, params);
        res.json({ success: true, count: result.rows.length, data: result.rows });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST record / collect payment
paymentsRouter.post('/collect', authenticate, async (req: Request, res: Response) => {
    try {
        const { customerId, amount, discount = 0.0, method = 'Cash', note = '' } = req.body;
        if (!customerId || (parseFloat(amount) <= 0 && parseFloat(discount) <= 0)) {
            return res.status(400).json({ success: false, error: 'Valid customerId and amount are required.' });
        }

        const result = await PaymentService.recordPayment({
            customerId,
            amount: parseFloat(amount) || 0.0,
            discount: parseFloat(discount) || 0.0,
            method,
            note
        });

        res.status(201).json({
            success: true,
            message: 'Payment recorded and applied to customer balance successfully.',
            data: result
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// DELETE payment and reverse balance & bill settlements
paymentsRouter.delete('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const result = await PaymentService.deletePaymentAndReverse(id);
        res.json({
            success: true,
            message: 'Payment deleted and customer balance/bills reversed successfully.',
            data: result
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
