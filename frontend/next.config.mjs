/** @type {import('next').NextConfig} */
const nextConfig = {
    async rewrites() {
        return process.env.NODE_ENV === 'development' ? [
            {
                source: '/api/:path*',
                destination: 'http://localhost:18082/api/:path*', // Proxy to Wiremock
            }
        ] : []
    },
};

export default nextConfig;
