import {Button, Divider, List, Stack, Typography} from "@mui/material";
import ButtonRow from "../../ButtonRow.tsx";
import CombinationComponentsPicker from "../../CombinationComponentsPicker.tsx";
import type {Dispatch, SetStateAction} from "react";

interface ChooseCombinationComponentsStepProps {
    savedDeveloperIds: number[],
    setSavedDeveloperIds: Dispatch<SetStateAction<number[]>>,

    savedStreamIds: number[],
    setSavedStreamIds: Dispatch<SetStateAction<number[]>>,

    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseCombinationComponentsStep({
                                                            savedDeveloperIds,
                                                            setSavedDeveloperIds,
                                                            savedStreamIds,
                                                            setSavedStreamIds,
                                                            updateForm
                                                        }: ChooseCombinationComponentsStepProps) {
    const selectionsPicked: boolean = savedDeveloperIds.length > 0 && savedStreamIds.length > 0

    return (
        <Stack gap={1}>
            <Typography variant="h5">Choose who is available and what is being worked on</Typography>
            <CombinationComponentsPicker
                savedDeveloperIds={savedDeveloperIds} setSavedDeveloperIds={setSavedDeveloperIds}
                savedStreamIds={savedStreamIds} setSavedStreamIds={setSavedStreamIds}/>
            <Divider/>
            <ButtonRow>
                <Button variant="contained" onClick={() => updateForm(prevState => prevState + 1)}
                        disabled={!selectionsPicked}>
                    See combinations
                    <List sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
        </Stack>
    )
}