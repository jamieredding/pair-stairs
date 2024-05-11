import type {Metadata} from "next";
import {inter} from "@/app/fonts";
import "./globals.css";
import Navbar from "@/components/Navbar";
import Toolbar from "@mui/material/Toolbar";
import RootContext from "@/components/RootContext";
import {CssBaseline} from "@mui/material";
import {ReactNode} from "react";

export const metadata: Metadata = {
    title: "Pair Stairs",
    description: "A tool for helping developers pair",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: ReactNode;
}>) {
    return (
        <html lang="en">
        <body className={inter.className}>
        <RootContext>
            <CssBaseline/>
            <Navbar/>
            {/*To pad the navbar above*/}
            <Toolbar/>
            {children}
        </RootContext>
        </body>
        </html>
    );
}
