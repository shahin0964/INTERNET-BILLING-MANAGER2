import { Router, Request, Response } from 'express';
import { query, withTransaction } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const syncRouter = Router();

/**
 * PULL changes from Server to Mobile App (since lastSyncTimestamp).
 */
syncRouter.get('/pull', authenticate, async (req: Request, res: Response) => {
    try {
        const since = parseInt(req.query.since as string, 10) || 0;

        const customers = await query(
            'SELECT * FROM customers WHERE created_date_millis > $1 OR EXTRACT(EPOCH FROM updated_at)*1000 > $1 ORDER BY id ASC',
            [since]
        );
        const packages = await query(
            'SELECT * FROM packages WHERE EXTRACT(EPOCH FROM updated_at)*1000 > $1 ORDER BY id ASC',
            [since]
        );
        const bills = await query(
            'SELECT * FROM bill_entries WHERE bill_date_millis > $1 OR EXTRACT(EPOCH FROM updated_at)*1000 > $1 ORDER BY id ASC',
            [since]
        );
        const payments = await query(
            'SELECT * FROM payments WHERE date_millis > $1 ORDER BY id ASC',
            [since]
        );
        const expenses = await query(
            'SELECT * FROM expenses WHERE date_millis > $1 ORDER BY id ASC',
            [since]
        );
        const settings = await query('SELECT * FROM isp_settings WHERE id = 1');

        res.json({
            success: true,
            serverTimestamp: Date.now(),
            data: {
                customers: customers.rows,
                packages: packages.rows,
                bills: bills.rows,
                payments: payments.rows,
                expenses: expenses.rows,
                settings: settings.rows[0] || null
            }
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

/**
 * PUSH mobile offline batch data to Server.
 */
syncRouter.post('/push', authenticate, async (req: Request, res: Response) => {
    try {
        const { customers = [], payments = [], bills = [], expenses = [] } = req.body;

        const results = await withTransaction(async (client) => {
            let insertedCustomers = 0;
            let insertedPayments = 0;
            let insertedBills = 0;
            let insertedExpenses = 0;

            // Upsert customers
            for (const c of customers) {
                await client.query(`
                    INSERT INTO customers 
                    (customer_code, name, pppoe_username, phone, package_id, package_name, speed_mbps, monthly_bill, due_amount, bill_due_day, status, connection_type, address, static_ip, notes)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
                    ON CONFLICT (id) DO UPDATE SET
                        name = EXCLUDED.name,
                        due_amount = EXCLUDED.due_amount,
                        status = EXCLUDED.status,
                        updated_at = CURRENT_TIMESTAMP
                `, [
                    c.customerCode || c.customer_code,
                    c.name,
                    c.pppoeUsername || c.pppoe_username,
                    c.phone,
                    c.packageId || c.package_id || null,
                    c.packageName || c.package_name,
                    c.speedMbps || c.speed_mbps || 0,
                    c.monthlyBill || c.monthly_bill || 0,
                    c.dueAmount || c.due_amount || 0,
                    c.billDueDay || c.bill_due_day || 10,
                    c.status || 'Active',
                    c.connectionType || c.connection_type || 'Fiber',
                    c.address || '',
                    c.staticIp || c.static_ip || '',
                    c.notes || ''
                ]);
                insertedCustomers++;
            }

            // Insert payments
            for (const p of payments) {
                await client.query(`
                    INSERT INTO payments 
                    (customer_id, customer_name, customer_code, amount, discount, payment_method, date_millis, month_year, note)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                `, [
                    p.customerId || p.customer_id,
                    p.customerName || p.customer_name,
                    p.customerCode || p.customer_code,
                    p.amount,
                    p.discount || 0,
                    p.paymentMethod || p.payment_method || 'Cash',
                    p.dateMillis || p.date_millis || Date.now(),
                    p.monthYear || p.month_year,
                    p.note || ''
                ]);
                insertedPayments++;
            }

            return {
                customersSynced: insertedCustomers,
                paymentsSynced: insertedPayments,
                billsSynced: insertedBills,
                expensesSynced: insertedExpenses
            };
        });

        res.json({
            success: true,
            serverTimestamp: Date.now(),
            results
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
