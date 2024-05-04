"use client"

import ChooseDevelopersForm from "@/app/daily-combination/components/ChooseDevelopersForm";
import {Fragment, useState} from "react";
import ChooseStreamsForm from "@/app/daily-combination/components/ChooseStreamsForm";
import ChooseCombinationForm from "@/app/daily-combination/components/ChooseCombinationForm";


export default function DailyCombinationPage() {
    const [savedDeveloperIds, setSavedDeveloperIds] = useState<number[]>()
    const [savedStreamIds, setSavedStreamIds] = useState<number[]>()
    const [formStage, setFormStage] = useState<number>(0)

    return (
        <Fragment>
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
        </Fragment>
    )
}