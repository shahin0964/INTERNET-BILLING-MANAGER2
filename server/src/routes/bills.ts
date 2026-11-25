import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { BillingService } from '../services/billingService.js';
import { authenticate } from '../middleware/auth.js';

export const billsRouter = Router();

// GET bills with filters
billsRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { monthYear, customerId, unpaidOnly } = req.query;
        let sql = `SELECT * FROM bill_entries`;
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

        if (unpaidOnly === 'true') {
            conditions.push(`is_paid = FALSE`);
        }

        if (conditions.length > 0) {
            sql += ` WHERE ` + conditions.join(' AND ');
        }

        sql += ` ORDER BY bill_date_millis DESC, id DESC`;

        const result = await query(sql, params);
        res.json({ success: true, count: result.rows.length, data: result.rows });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST generate monthly bills for all active customers
billsRouter.post('/generate-monthly', authenticate, async (req: Request, res: Response) => {
    try {
        const { monthYear } = req.body;
        const targetMonth = monthYear || `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;

        const result = await BillingService.generateMonthlyBills(targetMonth);
        res.json({
            success: true,
            message: `Bills successfully generated for ${result.generatedCount} customers (${result.skippedCount} already billed).`,
            data: result
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST add manual bill for single customer
billsRouter.post('/manual', authenticate, async (req: Request, res: Response) => {
    try {
        const { customerId, monthYear, amount, notes = '' } = req.body;
        if (!customerId || !amount) {
            return res.status(400).json({ success: false, error: 'CustomerId and amount are required.' });
        }

        const targetMonth = monthYear || `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;
        const result = await BillingService.addManualBill(customerId, targetMonth, parseFloat(amount), notes);

        res.status(201).json({
            success: true,
            message: 'Manual bill added successfully.',
            data: result
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
