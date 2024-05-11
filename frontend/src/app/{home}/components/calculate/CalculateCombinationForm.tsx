"use client"

import ChooseDevelopersStep from "@/app/{home}/components/calculate/ChooseDevelopersStep";
import {useState} from "react";
import ChooseStreamsStep from "@/app/{home}/components/calculate/ChooseStreamsStep";
import ChooseCombinationStep from "@/app/{home}/components/calculate/ChooseCombinationStep";
import {Stack} from "@mui/material";


export default function CalculateCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
    const [formStage, setFormStage] = useState<number>(0)

    return (
        <Stack gap={1}>
            {formStage === 0 &&
                <ChooseDevelopersStep savedDeveloperIds={savedDeveloperIds}
                                      setSavedDeveloperIds={setSavedDeveloperIds}
                                      updateForm={setFormStage}/>
            }
            {formStage === 1 &&
                <ChooseStreamsStep savedStreamIds={savedStreamIds}
                                   setSavedStreamIds={setSavedStreamIds}
                                   updateForm={setFormStage}/>
            }
            {formStage === 2 &&
                <ChooseCombinationStep developerIds={savedDeveloperIds as number[]}
                                       streamIds={savedStreamIds as number[]}
                                       updateForm={setFormStage}/>
            }
        </Stack>
    )
}