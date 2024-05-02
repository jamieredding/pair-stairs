"use client"

import {Card, CardContent} from "@mui/material";
import ChooseDevelopers from "@/components/ChooseDevelopers";
import Grid from "@mui/system/Unstable_Grid";
import {useState} from "react";
import ChooseStreams from "@/components/ChooseStreams";
import ChooseCombination from "@/components/ChooseCombination";
import LandingPlaceholder from "@/components/LandingPlaceholder";


export default function NewCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
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
                        {formStage === 2 &&
                            <ChooseCombination developerIds={savedDeveloperIds as number[]}
                                               streamIds={savedStreamIds as number[]}
                                               updateForm={setFormStage}/>
                        }
                        {formStage === 3 &&
                            <LandingPlaceholder/>
                        }
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
}