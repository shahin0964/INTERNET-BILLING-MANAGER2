import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const expensesRouter = Router();

// GET expenses with monthYear filter
expensesRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { monthYear } = req.query;
        let sql = `SELECT * FROM expenses`;
        const params: any[] = [];

        if (monthYear && typeof monthYear === 'string') {
            params.push(monthYear);
            sql += ` WHERE month_year = $1`;
        }

        sql += ` ORDER BY date_millis DESC, id DESC`;

        const result = await query(sql, params);
        res.json({ success: true, count: result.rows.length, data: result.rows });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// GET category summary
expensesRouter.get('/summary', authenticate, async (req: Request, res: Response) => {
    try {
        const { monthYear } = req.query;
        const targetMonth = monthYear || `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;

        const sumRes = await query(`
            SELECT category, SUM(amount) as total, COUNT(*) as count
            FROM expenses
            WHERE month_year = $1
            GROUP BY category
            ORDER BY total DESC
        `, [targetMonth]);

        const totalRes = await query(`
            SELECT 
                COALESCE(SUM(amount), 0) as total_outflow,
                COALESCE(MAX(amount), 0) as highest_expense,
                COALESCE(AVG(amount), 0) as average_expense,
                COUNT(*) as vouchers_count
            FROM expenses
            WHERE month_year = $1
        `, [targetMonth]);

        res.json({
            success: true,
            monthYear: targetMonth,
            overview: totalRes.rows[0],
            categories: sumRes.rows
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST create expense
expensesRouter.post('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { title, category = 'Other', amount, monthYear, note = '' } = req.body;
        if (!title || !amount) {
            return res.status(400).json({ success: false, error: 'Title and amount are required.' });
        }

        const date = new Date();
        const targetMonth = monthYear || `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        const now = Date.now();

        const insertRes = await query(
            `INSERT INTO expenses (title, category, amount, date_millis, month_year, note)
             VALUES ($1, $2, $3, $4, $5, $6) RETURNING *`,
            [title.trim(), category, parseFloat(amount), now, targetMonth, note]
        );

        res.status(201).json({ success: true, data: insertRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// DELETE expense
expensesRouter.delete('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        await query('DELETE FROM expenses WHERE id = $1', [id]);
        res.json({ success: true, message: 'Expense deleted successfully.' });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
