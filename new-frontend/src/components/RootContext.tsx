import type {PropsWithChildren} from "react";
import {LocalizationProvider} from "@mui/x-date-pickers";
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFns";
import {ThemeProvider} from "@mui/material";
import {theme} from "../theme.ts";

export default function RootContext({children}: PropsWithChildren) {
    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <ThemeProvider theme={theme}>
                {children}
            </ThemeProvider>
        </LocalizationProvider>
    )
}