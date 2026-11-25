import express from 'express';
import cors from 'cors';
import morgan from 'morgan';
import path from 'path';
import { fileURLToPath } from 'url';
import { config } from './config/index.js';
import { initializeDatabase } from './db/index.js';

// Route imports
import { authRouter } from './routes/auth.js';
import { dashboardRouter } from './routes/dashboard.js';
import { customersRouter } from './routes/customers.js';
import { packagesRouter } from './routes/packages.js';
import { billsRouter } from './routes/bills.js';
import { paymentsRouter } from './routes/payments.js';
import { expensesRouter } from './routes/expenses.js';
import { settingsRouter } from './routes/settings.js';
import { mikrotikRouter } from './routes/mikrotik.js';
import { syncRouter } from './routes/sync.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// Global Middlewares
app.use(cors({ origin: config.corsOrigin, credentials: true }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

if (config.nodeEnv !== 'test') {
    app.use(morgan('dev'));
}

// Health Check Endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'UP',
        app: 'Internet Billing Manager 2 - Central REST API',
        timestamp: new Date().toISOString(),
        version: '2.0.0'
    });
});

// Mount API Routes
app.use('/api/auth', authRouter);
app.use('/api/dashboard', dashboardRouter);
app.use('/api/customers', customersRouter);
app.use('/api/packages', packagesRouter);
app.use('/api/bills', billsRouter);
app.use('/api/payments', paymentsRouter);
app.use('/api/expenses', expensesRouter);
app.use('/api/settings', settingsRouter);
app.use('/api/mikrotik', mikrotikRouter);
app.use('/api/sync', syncRouter);

// Serve Web Frontend Static Files
const webPath = path.resolve(__dirname, '../../web');
app.use(express.static(webPath));

// Fallback for Single-Page Application (SPA) routing
app.get('*', (req, res, next) => {
    if (req.path.startsWith('/api')) {
        return next();
    }
    const indexPath = path.join(webPath, 'index.html');
    res.sendFile(indexPath, (err) => {
        if (err) {
            next();
        }
    });
});

// 404 Handler for API
app.use((req, res) => {
    res.status(404).json({ success: false, error: `API route '${req.originalUrl}' not found.` });
});

// Global Error Handler
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
    console.error('Unhandled Server Error:', err);
    res.status(err.status || 500).json({
        success: false,
        error: err.message || 'Internal Server Error'
    });
});

// Start Server & Initialize Database
async function startServer() {
    await initializeDatabase();
    
    app.listen(config.port, '0.0.0.0', () => {
        console.log(`\n======================================================`);
        console.log(`🚀 IBM 2 Server is running on: http://0.0.0.0:${config.port}`);
        console.log(`💻 Web Portal available at:   http://localhost:${config.port}`);
        console.log(`📡 REST API Base URL:         http://localhost:${config.port}/api`);
        console.log(`======================================================\n`);
    });
}

startServer();
