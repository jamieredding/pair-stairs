import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import tanstackRouter from "@tanstack/router-plugin/vite";

// https://vite.dev/config/
export default defineConfig(
    ({command, mode}) => {
        /** Only run the proxy while the dev-server is running */
        const useWiremock = command === 'serve' && mode === 'development';

        return {
            test: {
                environment: 'jsdom',
            },
            plugins: [
                tanstackRouter({
                    target: "react",
                    autoCodeSplitting: true,
                }),
                react()
            ],
            /** ---------- Dev server ---------- */
            server: useWiremock
                ? {
                    proxy: {
                        // forward every /api/* request to WireMock
                        '/api': {
                            target: 'http://localhost:18082',
                            changeOrigin: true,   // spoofs the Host header
                            secure: false,        // ignore self-signed HTTPS certs
                            // rewrite is optional here because the path is unchanged:
                            //   rewrite: p => p   //  /api/foo -> /api/foo
                        },
                    },
                }
                : {},
        }
    }
)
