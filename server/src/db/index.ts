import pg from 'pg';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { config } from '../config/index.js';

const { Pool } = pg;

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const isSslRequired = config.databaseUrl.includes('sslmode=require') || 
                      config.databaseUrl.includes('supabase.co') || 
                      config.databaseUrl.includes('neon.tech');

export const pool = new Pool({
    connectionString: config.databaseUrl,
    ssl: isSslRequired ? { rejectUnauthorized: false } : undefined,
    max: 20,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
});

pool.on('error', (err) => {
    console.error('Unexpected error on idle PostgreSQL client:', err);
});

export async function query(text: string, params?: any[]) {
    const start = Date.now();
    const res = await pool.query(text, params);
    const duration = Date.now() - start;
    if (config.nodeEnv === 'development') {
        console.log(`[DB Query] ${text.slice(0, 60)}... (${duration}ms, rows: ${res.rowCount})`);
    }
    return res;
}

export async function withTransaction<T>(callback: (client: pg.PoolClient) => Promise<T>): Promise<T> {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        const result = await callback(client);
        await client.query('COMMIT');
        return result;
    } catch (e) {
        await client.query('ROLLBACK');
        throw e;
    } finally {
        client.release();
    }
}

export async function initializeDatabase() {
    console.log('🔄 Checking & initializing PostgreSQL database schema...');
    try {
        const client = await pool.connect();
        try {
            // Find schema.sql file
            let schemaPath = path.resolve(__dirname, '../../sql/schema.sql');
            if (!fs.existsSync(schemaPath)) {
                schemaPath = path.resolve(__dirname, '../../../sql/schema.sql');
            }
            if (fs.existsSync(schemaPath)) {
                const schemaSql = fs.readFileSync(schemaPath, 'utf8');
                await client.query(schemaSql);
                console.log('✅ PostgreSQL database schema verified and up-to-date.');
            } else {
                console.warn('⚠️ schema.sql file not found at:', schemaPath);
            }
        } finally {
            client.release();
        }
    } catch (err: any) {
        console.error('⚠️ Could not connect to PostgreSQL:', err.message);
        console.log('ℹ️ Running in API mode. Please ensure PostgreSQL is running at DATABASE_URL.');
    }
}
