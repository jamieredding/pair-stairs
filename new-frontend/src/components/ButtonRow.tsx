import {Stack} from "@mui/material";
import type {PropsWithChildren} from "react";

export default function ButtonRow({children}: PropsWithChildren) {
    return (
        <Stack spacing={1} flexWrap="wrap" direction="row" useFlexGap={true}>
            {children}
        </Stack>
    )
}