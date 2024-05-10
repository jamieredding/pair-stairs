"use client"

import ChooseDevelopersForm from "@/app/daily-combination/components/ChooseDevelopersForm";
import {useState} from "react";
import ChooseStreamsForm from "@/app/daily-combination/components/ChooseStreamsForm";
import ChooseCombinationForm from "@/app/daily-combination/components/ChooseCombinationForm";
import {Stack, Typography} from "@mui/material";


export default function DailyCombinationPage() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
    const [formStage, setFormStage] = useState<number>(0)

    return (
        <Stack gap={1}>
            <Typography variant="h4">Calculate Combination</Typography>
            {formStage === 0 &&
                <ChooseDevelopersForm savedDeveloperIds={savedDeveloperIds}
                                      setSavedDeveloperIds={setSavedDeveloperIds}
                                      updateForm={setFormStage}/>
            }
            {formStage === 1 &&
                <ChooseStreamsForm savedStreamIds={savedStreamIds}
                                   setSavedStreamIds={setSavedStreamIds}
                                   updateForm={setFormStage}/>
            }
            {formStage === 2 &&
                <ChooseCombinationForm developerIds={savedDeveloperIds as number[]}
                                       streamIds={savedStreamIds as number[]}
                                       updateForm={setFormStage}/>
            }
        </Stack>
    )
}