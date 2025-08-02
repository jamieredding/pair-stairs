"use client"

import {PropsWithChildren} from "react";
import {LocalizationProvider} from "@mui/x-date-pickers";
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";
import {ThemeProvider} from "@mui/material";
import {theme} from "@/app/theme";

export default function RootContext({children}: PropsWithChildren) {
    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <ThemeProvider theme={theme}>
                {children}
            </ThemeProvider>
        </LocalizationProvider>
    )
}