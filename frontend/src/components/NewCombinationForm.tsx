"use client"

import ChooseDevelopers from "@/components/ChooseDevelopers";
import {Fragment, useState} from "react";
import ChooseStreams from "@/components/ChooseStreams";
import ChooseCombination from "@/components/ChooseCombination";


export default function NewCombinationForm() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
    const [formStage, setFormStage] = useState<number>(0)

    return (
        <Fragment>
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
        </Fragment>
    )
}