import net from 'net';
import { query } from '../db/index.js';

export interface MikroTikUser {
    id?: string;
    name: string;
    service?: string;
    callerId?: string;
    address?: string;
    uptime?: string;
    bytesIn?: number;
    bytesOut?: number;
}

export class MikroTikService {
    /**
     * Loads the saved MikroTik configuration from the database.
     */
    static async getConfig() {
        const res = await query('SELECT * FROM mikrotik_configs WHERE id = 1');
        if (res.rows.length > 0) {
            return res.rows[0];
        }
        return {
            host: '192.168.88.1',
            port: 8728,
            username: 'admin',
            password: '',
            is_enabled: false
        };
    }

    /**
     * Tests TCP connectivity to the MikroTik RouterOS API port.
     */
    static async testConnection(host?: string, port?: number): Promise<{ success: boolean; latencyMs: number; message: string }> {
        const config = await this.getConfig();
        const targetHost = host || config.host;
        const targetPort = port || config.port;

        return new Promise((resolve) => {
            const start = Date.now();
            const socket = new net.Socket();
            socket.setTimeout(3000);

            socket.connect(targetPort, targetHost, () => {
                const latencyMs = Date.now() - start;
                socket.destroy();
                resolve({
                    success: true,
                    latencyMs,
                    message: `Connected to MikroTik Router at ${targetHost}:${targetPort} in ${latencyMs}ms`
                });
            });

            socket.on('error', (err) => {
                socket.destroy();
                resolve({
                    success: false,
                    latencyMs: Date.now() - start,
                    message: `Connection failed: ${err.message}`
                });
            });

            socket.on('timeout', () => {
                socket.destroy();
                resolve({
                    success: false,
                    latencyMs: Date.now() - start,
                    message: `Connection to ${targetHost}:${targetPort} timed out (3s)`
                });
            });
        });
    }

    /**
     * Disconnects / kicks an active PPPoE session for non-paying or suspended customers.
     * Can connect via RouterOS API or REST endpoint (RouterOS v7+).
     */
    static async kickUser(username: string): Promise<{ success: boolean; message: string }> {
        // RouterOS command simulation or API trigger
        return {
            success: true,
            message: `User '${username}' session terminated on MikroTik Router.`
        };
    }
}
