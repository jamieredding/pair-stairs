import {defineConfig} from 'vitest/config'
import react from '@vitejs/plugin-react'
import {join} from "pathe";

export default defineConfig({
    plugins: [react()],
    test: {
        environment: 'jsdom',
    },
    resolve: {
        alias: {
            '@': join(__dirname, './src'),
        },
    }
})