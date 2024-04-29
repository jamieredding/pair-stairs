import type {Metadata} from "next";
import {inter} from "@/app/fonts";
import "./globals.css";

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
      <body className={inter.className}>{children}</body>
    </html>
  );
}
