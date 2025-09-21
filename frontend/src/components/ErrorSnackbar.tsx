import {Alert, Snackbar} from "@mui/material";
import {type ReactElement, useEffect, useRef, useState} from "react";
import type {ApiError} from "../domain/ApiError.ts";
import Error from "./Error.tsx";

export interface ErrorSnackbarProps {
    error: ApiError | undefined;
    alertContent: (errorCode: string) => ReactElement;
}

export default function ErrorSnackbar({error, alertContent}: ErrorSnackbarProps) {
    const [errorSnackbarOpen, setErrorSnackbarOpen] = useState(false)
    const prevErrorRef = useRef<ApiError | null>(null);

    // Open snackbar when a *new* error appears
    useEffect(() => {
        const prev = prevErrorRef.current;
        if (error && error !== prev) {
            setErrorSnackbarOpen(true);
        }
        prevErrorRef.current = (error as ApiError | null) ?? null;
    }, [error]);

    if (error === undefined) {
        return <></>
    }

    return (
        <>
            <Error/>
            <Snackbar
                open={errorSnackbarOpen}
                onClose={() => setErrorSnackbarOpen(false)}
                anchorOrigin={{vertical: "bottom", horizontal: "center"}}
            >
                <div>
                    <Alert severity="error" onClose={() => setErrorSnackbarOpen(false)}>
                        {alertContent(error?.errorCode ?? "UNKNOWN")}
                    </Alert>
                </div>
            </Snackbar>
        </>
    )
}