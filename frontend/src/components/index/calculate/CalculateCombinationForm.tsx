import {useState} from "react";
import {Stack} from "@mui/material";
import ChooseDevelopersStep from "./ChooseDevelopersStep.tsx";
import ChooseStreamsStep from "./ChooseStreamsStep.tsx";
import ChooseCombinationStep from "./ChooseCombinationStep.tsx";


export default function CalculateCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
    const [formStage, setFormStage] = useState<number>(0)

    function resetForm() {
        setSavedDeveloperIds(undefined)
        setSavedStreamIds(undefined)
        setFormStage(0)
    }

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
                                       updateForm={setFormStage}
                                       resetForm={resetForm}/>
            }
        </Stack>
    )
}