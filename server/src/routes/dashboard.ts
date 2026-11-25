import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const dashboardRouter = Router();

dashboardRouter.get('/stats', authenticate, async (req: Request, res: Response) => {
    try {
        const date = new Date();
        const currentMonthYear = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        // Start and end of today in millis
        const startOfToday = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
        const endOfToday = startOfToday + 86400000 - 1;

        // 1. Customer counts
        const custCountRes = await query(`
            SELECT 
                COUNT(*) as total,
                COUNT(*) FILTER (WHERE status = 'Active') as active,
                COUNT(*) FILTER (WHERE status = 'Suspended') as suspended,
                COUNT(*) FILTER (WHERE status = 'Inactive') as inactive,
                COALESCE(SUM(monthly_bill) FILTER (WHERE status = 'Active'), 0) as total_active_monthly_bill,
                COALESCE(SUM(due_amount), 0) as total_outstanding_due
            FROM customers
        `);
        const custStats = custCountRes.rows[0];

        // 2. Collections (This Month & Today)
        const paymentStatsRes = await query(`
            SELECT 
                COALESCE(SUM(amount) FILTER (WHERE month_year = $1), 0) as collected_this_month,
                COALESCE(SUM(discount) FILTER (WHERE month_year = $1), 0) as discount_this_month,
                COALESCE(SUM(amount) FILTER (WHERE date_millis >= $2 AND date_millis <= $3), 0) as collected_today
            FROM payments
        `, [currentMonthYear, startOfToday, endOfToday]);
        const paymentStats = paymentStatsRes.rows[0];

        // 3. Expenses (This Month)
        const expenseStatsRes = await query(`
            SELECT 
                COALESCE(SUM(amount) FILTER (WHERE month_year = $1), 0) as expense_this_month,
                COALESCE(SUM(amount) FILTER (WHERE month_year = $1 AND (category = 'Bandwidth Bill' OR category ILIKE '%bandwidth%')), 0) as bandwidth_bill_this_month
            FROM expenses
        `, [currentMonthYear]);
        const expenseStats = expenseStatsRes.rows[0];

        // 4. Recent 10 payments
        const recentPaymentsRes = await query(`
            SELECT * FROM payments 
            ORDER BY date_millis DESC 
            LIMIT 10
        `);

        // 5. Total Billed this month
        const billStatsRes = await query(`
            SELECT COALESCE(SUM(bill_amount), 0) as billed_this_month
            FROM bill_entries
            WHERE month_year = $1
        `, [currentMonthYear]);
        const billStats = billStatsRes.rows[0];

        const collectedMonth = parseFloat(paymentStats.collected_this_month);
        const expenseMonth = parseFloat(expenseStats.expense_this_month);
        const monthlyProfit = collectedMonth - expenseMonth;

        res.json({
            success: true,
            data: {
                currentMonthYear,
                customers: {
                    total: parseInt(custStats.total, 10),
                    active: parseInt(custStats.active, 10),
                    suspended: parseInt(custStats.suspended, 10),
                    inactive: parseInt(custStats.inactive, 10),
                    totalActiveMonthlyBill: parseFloat(custStats.total_active_monthly_bill),
                    totalOutstandingDue: parseFloat(custStats.total_outstanding_due)
                },
                financials: {
                    billedThisMonth: parseFloat(billStats.billed_this_month) || parseFloat(custStats.total_active_monthly_bill),
                    collectedThisMonth: collectedMonth,
                    discountThisMonth: parseFloat(paymentStats.discount_this_month),
                    collectedToday: parseFloat(paymentStats.collected_today),
                    expensesThisMonth: expenseMonth,
                    bandwidthBillThisMonth: parseFloat(expenseStats.bandwidth_bill_this_month),
                    monthlyProfit
                },
                recentPayments: recentPaymentsRes.rows
            }
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
