import { Router, Request, Response } from 'express';
import { query } from '../db/index.js';
import { authenticate } from '../middleware/auth.js';

export const customersRouter = Router();

// GET all customers with search and status filter
customersRouter.get('/', authenticate, async (req: Request, res: Response) => {
    try {
        const { q, status } = req.query;
        let sql = `SELECT * FROM customers`;
        const params: any[] = [];
        const conditions: string[] = [];

        if (status && status !== 'All') {
            params.push(status);
            conditions.push(`status = $${params.length}`);
        }

        if (q && typeof q === 'string' && q.trim()) {
            params.push(`%${q.trim()}%`);
            conditions.push(`(name ILIKE $${params.length} OR customer_code ILIKE $${params.length} OR pppoe_username ILIKE $${params.length} OR phone ILIKE $${params.length})`);
        }

        if (conditions.length > 0) {
            sql += ` WHERE ` + conditions.join(' AND ');
        }

        sql += ` ORDER BY id DESC`;

        const result = await query(sql, params);
        res.json({ success: true, count: result.rows.length, data: result.rows });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// GET single customer by ID with full billing history
customersRouter.get('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const custRes = await query('SELECT * FROM customers WHERE id = $1', [id]);
        if (custRes.rows.length === 0) {
            return res.status(404).json({ success: false, error: 'Customer not found' });
        }

        const customer = custRes.rows[0];
        const billsRes = await query('SELECT * FROM bill_entries WHERE customer_id = $1 ORDER BY month_year DESC', [id]);
        const paymentsRes = await query('SELECT * FROM payments WHERE customer_id = $1 ORDER BY date_millis DESC', [id]);

        res.json({
            success: true,
            data: {
                customer,
                bills: billsRes.rows,
                payments: paymentsRes.rows
            }
        });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// POST create customer
customersRouter.post('/', authenticate, async (req: Request, res: Response) => {
    try {
        const {
            name,
            phone,
            pppoeUsername,
            packageId,
            packageName,
            speedMbps,
            monthlyBill,
            dueAmount = 0.0,
            billDueDay = 10,
            status = 'Active',
            connectionType = 'Fiber',
            address = '',
            staticIp = '',
            notes = ''
        } = req.body;

        if (!name && !pppoeUsername) {
            return res.status(400).json({ success: false, error: 'Customer name or PPPoE username is required.' });
        }

        // Auto-generate customerCode if not supplied (e.g. U001, U002)
        let customerCode = req.body.customerCode;
        if (!customerCode) {
            const countRes = await query('SELECT COUNT(*) FROM customers');
            const nextIndex = parseInt(countRes.rows[0].count, 10) + 1;
            customerCode = `U${String(nextIndex).padStart(3, '0')}`;
        }

        const cleanUsername = pppoeUsername || name.toLowerCase().replace(/\s+/g, '');

        const insertRes = await query(
            `INSERT INTO customers 
             (customer_code, name, pppoe_username, phone, package_id, package_name, speed_mbps, monthly_bill, due_amount, bill_due_day, status, connection_type, address, static_ip, notes)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
             RETURNING *`,
            [
                customerCode,
                name || cleanUsername,
                cleanUsername,
                phone || '',
                packageId || null,
                packageName || '',
                speedMbps || 0,
                monthlyBill || 0.0,
                dueAmount || 0.0,
                billDueDay || 10,
                status,
                connectionType,
                address,
                staticIp,
                notes
            ]
        );

        res.status(201).json({ success: true, data: insertRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// PUT update customer
customersRouter.put('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const {
            name,
            customerCode,
            pppoeUsername,
            phone,
            packageId,
            packageName,
            speedMbps,
            monthlyBill,
            dueAmount,
            billDueDay,
            status,
            connectionType,
            address,
            staticIp,
            notes
        } = req.body;

        const updateRes = await query(
            `UPDATE customers SET
                name = COALESCE($1, name),
                customer_code = COALESCE($2, customer_code),
                pppoe_username = COALESCE($3, pppoe_username),
                phone = COALESCE($4, phone),
                package_id = $5,
                package_name = COALESCE($6, package_name),
                speed_mbps = COALESCE($7, speed_mbps),
                monthly_bill = COALESCE($8, monthly_bill),
                due_amount = COALESCE($9, due_amount),
                bill_due_day = COALESCE($10, bill_due_day),
                status = COALESCE($11, status),
                connection_type = COALESCE($12, connection_type),
                address = COALESCE($13, address),
                static_ip = COALESCE($14, static_ip),
                notes = COALESCE($15, notes),
                updated_at = CURRENT_TIMESTAMP
             WHERE id = $16 RETURNING *`,
            [
                name,
                customerCode,
                pppoeUsername,
                phone,
                packageId || null,
                packageName,
                speedMbps,
                monthlyBill,
                dueAmount,
                billDueDay,
                status,
                connectionType,
                address,
                staticIp,
                notes,
                id
            ]
        );

        if (updateRes.rows.length === 0) {
            return res.status(404).json({ success: false, error: 'Customer not found' });
        }

        res.json({ success: true, data: updateRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// PATCH customer status (e.g. Active, Suspended, Inactive)
customersRouter.patch('/:id/status', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const { status } = req.body;
        if (!status) {
            return res.status(400).json({ success: false, error: 'Status is required' });
        }

        const updateRes = await query(
            `UPDATE customers SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING *`,
            [status, id]
        );

        if (updateRes.rows.length === 0) {
            return res.status(404).json({ success: false, error: 'Customer not found' });
        }

        res.json({ success: true, data: updateRes.rows[0] });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// DELETE customer
customersRouter.delete('/:id', authenticate, async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        await query('DELETE FROM customers WHERE id = $1', [id]);
        res.json({ success: true, message: 'Customer deleted successfully' });
    } catch (err: any) {
        res.status(500).json({ success: false, error: err.message });
    }
});
