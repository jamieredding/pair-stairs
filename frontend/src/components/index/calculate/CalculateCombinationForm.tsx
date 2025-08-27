import {useState} from "react";
import {Stack} from "@mui/material";
import ChooseCombinationStep from "./ChooseCombinationStep.tsx";
import ChooseCombinationComponentsStep from "./ChooseCombinationComponentsStep.tsx";


export default function CalculateCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>([])
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>([])
    const [formStage, setFormStage] = useState<number>(0)

    function resetForm() {
        setSavedDeveloperIds([])
        setSavedStreamIds([])
        setFormStage(0)
    }

    return (
        <Stack gap={1}>
            {formStage === 0 &&
                <ChooseCombinationComponentsStep
                    savedDeveloperIds={savedDeveloperIds} setSavedDeveloperIds={setSavedDeveloperIds}
                    savedStreamIds={savedStreamIds} setSavedStreamIds={setSavedStreamIds}
                    updateForm={setFormStage}/>
            }
            {formStage === 1 &&
                <ChooseCombinationStep developerIds={savedDeveloperIds}
                                       streamIds={savedStreamIds}
                                       updateForm={setFormStage}
                                       resetForm={resetForm}/>
            }
        </Stack>
    )
}