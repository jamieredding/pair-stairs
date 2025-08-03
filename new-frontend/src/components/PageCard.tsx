import {Grid} from "@mui/system";
import {Card, CardContent} from "@mui/material";
import type {PropsWithChildren} from "react";

export default function PageCard({children}: PropsWithChildren) {
    return (
        <Grid container justifyContent="center">
            <Grid size={{xs: 12, sm: 4}}>
                <Card>
                    <CardContent>
                        {children}
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
};