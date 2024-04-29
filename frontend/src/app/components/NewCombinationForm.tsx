"use client"

import {Card, CardContent} from "@mui/material";
import ChooseDevelopers from "@/app/components/ChooseDevelopers";
import Grid from "@mui/system/Unstable_Grid";
import {useState} from "react";


export default function NewCombinationForm() {
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>([])

    return (
        <Grid container justifyContent="center">
            <Grid xs={12} sm={4}>
                <Card>
                    <CardContent>
                        <ChooseDevelopers savedDeveloperIds={selectedDeveloperIds}
                                          setSavedDeveloperIds={setSelectedDeveloperIds}/>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
}