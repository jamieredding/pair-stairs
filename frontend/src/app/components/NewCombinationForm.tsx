"use client"

import {Card, CardContent} from "@mui/material";
import ChooseDevelopers from "@/app/components/ChooseDevelopers";
import Grid from "@mui/system/Unstable_Grid";
import {useState} from "react";
import ChooseStreams from "@/app/components/ChooseStreams";


export default function NewCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>([])
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>([])
    const [formStage, setFormStage] = useState<number>(0)

    return (
        <Grid container justifyContent="center">
            <Grid xs={12} sm={4}>
                <Card>
                    <CardContent>
                        {formStage === 0 &&
                            <ChooseDevelopers savedDeveloperIds={savedDeveloperIds}
                                              setSavedDeveloperIds={setSavedDeveloperIds}
                                              updateForm={setFormStage}/>
                        }
                        {formStage === 1 &&
                            <ChooseStreams savedStreamIds={savedStreamIds}
                                           setSavedStreamIds={setSavedStreamIds}
                            updateForm={setFormStage}/>
                        }
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
}