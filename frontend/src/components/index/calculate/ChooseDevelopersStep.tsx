import {Button, Divider, Stack, Typography} from "@mui/material";
import {useEffect, useState} from "react";
import {ArrowForward} from "@mui/icons-material";
import useDeveloperInfos from "../../../hooks/developers/useDeveloperInfos.ts";
import Loading from "../../Loading.tsx";
import Error from "../../Error.tsx";
import IdToggleButtonGroup from "../../IdToggleButtonGroup.tsx";
import ButtonRow from "../../ButtonRow.tsx";

interface ChooseDeveloperStepProps {
    savedDeveloperIds?: number[],
    setSavedDeveloperIds: (newDeveloperIds: number[]) => void,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseDevelopersStep({
                                                 savedDeveloperIds,
                                                 setSavedDeveloperIds,
                                                 updateForm
                                             }: ChooseDeveloperStepProps) {
    const {allDevelopers, isError, isLoading} = useDeveloperInfos();
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>(() => savedDeveloperIds || []);
    const dataLoaded: boolean = allDevelopers !== undefined

    useEffect(() => {
        if (allDevelopers) {
            setSelectedDeveloperIds(allDevelopers.map(dev => dev.id))
        }
    }, [allDevelopers]);

    function progressForm() {
        setSavedDeveloperIds(selectedDeveloperIds);
        updateForm(prevState => prevState + 1)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h5">Who is in today?</Typography>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allDevelopers &&
                <IdToggleButtonGroup allItems={allDevelopers} selectedIds={selectedDeveloperIds}
                                     setSelectedIds={setSelectedDeveloperIds}/>
            }
            <Divider/>
            <ButtonRow>
                <Button variant="contained"
                        onClick={progressForm}
                        disabled={!dataLoaded}
                >
                    Next
                    <ArrowForward sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
        </Stack>
    )
}