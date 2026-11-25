import { withTransaction } from '../db/index.js';

export interface RecordPaymentParams {
    customerId: number | string;
    amount: number;
    discount?: number;
    method?: string;
    note?: string;
}

export class PaymentService {
    /**
     * Records a payment with FIFO oldest-bill settlement.
     * Mirrors the exact logic from Android BillingRepository.recordPayment.
     */
    static async recordPayment(params: RecordPaymentParams) {
        const { customerId, amount, discount = 0, method = 'Cash', note = '' } = params;

        return await withTransaction(async (client) => {
            // 1. Get customer
            const custRes = await client.query(`SELECT * FROM customers WHERE id = $1`, [customerId]);
            if (custRes.rows.length === 0) {
                throw new Error('Customer not found');
            }
            const customer = custRes.rows[0];

            const date = new Date();
            const currentMonthYear = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
            const now = Date.now();

            // 2. Insert Payment Transaction
            const paymentRes = await client.query(
                `INSERT INTO payments 
                 (customer_id, customer_name, customer_code, amount, discount, payment_method, date_millis, month_year, note)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING *`,
                [
                    customerId,
                    customer.name || customer.pppoe_username,
                    customer.customer_code,
                    amount,
                    discount,
                    method,
                    now,
                    currentMonthYear,
                    note
                ]
            );
            const transaction = paymentRes.rows[0];

            const totalDeduction = amount + discount;
            const currentDue = parseFloat(customer.due_amount) || 0;
            const newDue = Math.max(0, currentDue - totalDeduction);

            // 3. Update customer due_amount
            await client.query(
                `UPDATE customers SET due_amount = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2`,
                [newDue, customerId]
            );

            // 4. Fetch unpaid bills in chronological order (oldest first - FIFO)
            const unpaidRes = await client.query(
                `SELECT * FROM bill_entries 
                 WHERE customer_id = $1 AND is_paid = FALSE 
                 ORDER BY month_year ASC, id ASC`,
                [customerId]
            );
            const unpaidBills = unpaidRes.rows;
            const pastUnpaidBills = unpaidBills.filter(b => b.month_year < currentMonthYear);
            const currentMonthBill = unpaidBills.find(b => b.month_year === currentMonthYear);

            let remainingPayment = totalDeduction;

            // Step A: Pay past recorded bills first (oldest first)
            for (const bill of pastUnpaidBills) {
                if (remainingPayment <= 0) break;
                const billDue = parseFloat(bill.due_amount) || 0;
                if (billDue > 0) {
                    const payThis = Math.min(remainingPayment, billDue);
                    const newPaid = parseFloat(bill.paid_amount) + payThis;
                    const newBillDue = Math.max(0, parseFloat(bill.bill_amount) - newPaid);
                    const isPaid = newBillDue <= 0.001;

                    await client.query(
                        `UPDATE bill_entries 
                         SET paid_amount = $1, due_amount = $2, is_paid = $3, updated_at = CURRENT_TIMESTAMP 
                         WHERE id = $4`,
                        [newPaid, newBillDue, isPaid, bill.id]
                    );
                    remainingPayment -= payThis;
                }
            }

            // Step B: Absorbing unrecorded past dues before touching current month bill
            const currentBillDueAmount = currentMonthBill ? parseFloat(currentMonthBill.due_amount) : (parseFloat(customer.monthly_bill) || 0);
            const initialPreviousDue = Math.max(0, currentDue - currentBillDueAmount);
            const pastRecordedDueSum = pastUnpaidBills.reduce((acc, b) => acc + (parseFloat(b.due_amount) || 0), 0);
            const unrecordedPreviousDue = Math.max(0, initialPreviousDue - pastRecordedDueSum);

            if (unrecordedPreviousDue > 0 && remainingPayment > 0) {
                const payUnrecorded = Math.min(remainingPayment, unrecordedPreviousDue);
                remainingPayment -= payUnrecorded;
            }

            // Step C: Apply remainder to running month's bill
            if (currentMonthBill && remainingPayment > 0) {
                const billDue = parseFloat(currentMonthBill.due_amount) || 0;
                const payThis = Math.min(remainingPayment, billDue);
                const newPaid = parseFloat(currentMonthBill.paid_amount) + payThis;
                const newBillDue = Math.max(0, parseFloat(currentMonthBill.bill_amount) - newPaid);
                const isPaid = newBillDue <= 0.001;

                await client.query(
                    `UPDATE bill_entries 
                     SET paid_amount = $1, due_amount = $2, is_paid = $3, updated_at = CURRENT_TIMESTAMP 
                     WHERE id = $4`,
                    [newPaid, newBillDue, isPaid, currentMonthBill.id]
                );
                remainingPayment -= payThis;
            }

            return {
                transaction,
                newDue
            };
        });
    }

    /**
     * Deletes a payment and reverses applied bill deductions (LIFO order).
     */
    static async deletePaymentAndReverse(paymentId: number | string) {
        return await withTransaction(async (client) => {
            const payRes = await client.query(`SELECT * FROM payments WHERE id = $1`, [paymentId]);
            if (payRes.rows.length === 0) {
                throw new Error('Payment not found');
            }
            const payment = payRes.rows[0];

            await client.query(`DELETE FROM payments WHERE id = $1`, [paymentId]);

            const custRes = await client.query(`SELECT * FROM customers WHERE id = $1`, [payment.customer_id]);
            if (custRes.rows.length === 0) return { deleted: true };
            const customer = custRes.rows[0];

            const totalToRestore = parseFloat(payment.amount) + parseFloat(payment.discount);
            const restoredDue = (parseFloat(customer.due_amount) || 0) + totalToRestore;

            await client.query(
                `UPDATE customers SET due_amount = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2`,
                [restoredDue, customer.id]
            );

            // Reverse bill payments in LIFO order (newest paid first)
            const billsRes = await client.query(
                `SELECT * FROM bill_entries WHERE customer_id = $1 ORDER BY month_year DESC, id DESC`,
                [customer.id]
            );
            const customerBills = billsRes.rows;

            let remainingToReverse = totalToRestore;
            for (const bill of customerBills) {
                if (remainingToReverse <= 0) break;
                const paid = parseFloat(bill.paid_amount) || 0;
                if (paid > 0) {
                    const reverseThis = Math.min(remainingToReverse, paid);
                    const newPaid = Math.max(0, paid - reverseThis);
                    const newDue = Math.max(0, parseFloat(bill.bill_amount) - newPaid);
                    const isPaid = newDue <= 0.001;

                    await client.query(
                        `UPDATE bill_entries 
                         SET paid_amount = $1, due_amount = $2, is_paid = $3, updated_at = CURRENT_TIMESTAMP 
                         WHERE id = $4`,
                        [newPaid, newDue, isPaid, bill.id]
                    );
                    remainingToReverse -= reverseThis;
                }
            }

            return { deleted: true, restoredDue };
        });
    }
}
