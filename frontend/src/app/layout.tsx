import type {Metadata} from "next";
import {inter} from "@/app/fonts";
import "./globals.css";
import Navbar from "@/components/Navbar";
import Toolbar from "@mui/material/Toolbar";

export const metadata: Metadata = {
    title: "Pair Stairs",
    description: "A tool for helping developers pair",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
        <body className={inter.className}>
        <Navbar/>
        {/*To pad the navbar above*/}
        <Toolbar/>
        {children}
        </body>
        </html>
    );
}
