import {PropsWithChildren} from "react";
import Grid from "@mui/system/Unstable_Grid";
import {Card, CardContent} from "@mui/material";

export default function PageCard({children}: PropsWithChildren) {
    return (
        <Grid container justifyContent="center">
            <Grid xs={12} sm={4}>
                <Card>
                    <CardContent>
                        {children}
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
};