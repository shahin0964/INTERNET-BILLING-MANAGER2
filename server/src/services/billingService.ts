import { query, withTransaction } from '../db/index.js';

export interface GenerateBillsResult {
    monthYear: string;
    generatedCount: number;
    skippedCount: number;
    totalAmount: number;
}

export class BillingService {
    /**
     * Atomically generates monthly bills for all Active customers who have not yet been billed for monthYear.
     * Updates customer dueAmount and inserts bill_entries in a single database transaction.
     */
    static async generateMonthlyBills(monthYear: string): Promise<GenerateBillsResult> {
        return await withTransaction(async (client) => {
            // 1. Get all Active customers
            const customersRes = await client.query(
                `SELECT id, customer_code, name, pppoe_username, monthly_bill, due_amount 
                 FROM customers 
                 WHERE status = 'Active'`
            );
            const activeCustomers = customersRes.rows;

            // 2. Get customer IDs already billed for this monthYear
            const billedRes = await client.query(
                `SELECT customer_id FROM bill_entries WHERE month_year = $1`,
                [monthYear]
            );
            const alreadyBilledIds = new Set(billedRes.rows.map(r => r.customer_id.toString()));

            const toBill = activeCustomers.filter(c => !alreadyBilledIds.has(c.id.toString()));
            let totalAmount = 0;
            const now = Date.now();

            for (const cust of toBill) {
                const billAmount = parseFloat(cust.monthly_bill) || 0;
                const currentDue = parseFloat(cust.due_amount) || 0;
                const newDue = currentDue + billAmount;

                // Update customer dueAmount
                await client.query(
                    `UPDATE customers SET due_amount = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2`,
                    [newDue, cust.id]
                );

                // Insert bill_entry
                await client.query(
                    `INSERT INTO bill_entries 
                     (customer_id, customer_name, customer_code, month_year, bill_amount, paid_amount, due_amount, is_paid, bill_date_millis, notes)
                     VALUES ($1, $2, $3, $4, $5, 0.00, $5, FALSE, $6, $7)`,
                    [
                        cust.id,
                        cust.name || cust.pppoe_username,
                        cust.customer_code,
                        monthYear,
                        billAmount,
                        now,
                        `Monthly bill for ${monthYear}`
                    ]
                );

                totalAmount += billAmount;
            }

            return {
                monthYear,
                generatedCount: toBill.length,
                skippedCount: activeCustomers.length - toBill.length,
                totalAmount
            };
        });
    }

    /**
     * Adds a manual bill for a customer (e.g. connection charge, static IP, or custom fee)
     */
    static async addManualBill(customerId: number | string, monthYear: string, amount: number, notes: string) {
        return await withTransaction(async (client) => {
            const custRes = await client.query(`SELECT * FROM customers WHERE id = $1`, [customerId]);
            if (custRes.rows.length === 0) {
                throw new Error('Customer not found');
            }
            const cust = custRes.rows[0];
            const currentDue = parseFloat(cust.due_amount) || 0;
            const updatedDue = currentDue + amount;

            await client.query(`UPDATE customers SET due_amount = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2`, [updatedDue, customerId]);

            // Check if bill exists for this month
            const existingBillRes = await client.query(
                `SELECT * FROM bill_entries WHERE customer_id = $1 AND month_year = $2 LIMIT 1`,
                [customerId, monthYear]
            );

            let billId: string;
            const now = Date.now();

            if (existingBillRes.rows.length > 0) {
                const existing = existingBillRes.rows[0];
                const newBillAmount = parseFloat(existing.bill_amount) + amount;
                const newBillDue = parseFloat(existing.due_amount) + amount;

                await client.query(
                    `UPDATE bill_entries 
                     SET bill_amount = $1, due_amount = $2, is_paid = FALSE, bill_date_millis = $3, notes = $4, updated_at = CURRENT_TIMESTAMP
                     WHERE id = $5`,
                    [newBillAmount, newBillDue, now, notes || existing.notes, existing.id]
                );
                billId = existing.id;
            } else {
                const insertRes = await client.query(
                    `INSERT INTO bill_entries 
                     (customer_id, customer_name, customer_code, month_year, bill_amount, paid_amount, due_amount, is_paid, bill_date_millis, notes)
                     VALUES ($1, $2, $3, $4, $5, 0.00, $5, FALSE, $6, $7) RETURNING id`,
                    [
                        customerId,
                        cust.name || cust.pppoe_username,
                        cust.customer_code,
                        monthYear,
                        amount,
                        now,
                        notes || `Bill for ${monthYear}`
                    ]
                );
                billId = insertRes.rows[0].id;
            }

            return { billId, updatedDue };
        });
    }
}
