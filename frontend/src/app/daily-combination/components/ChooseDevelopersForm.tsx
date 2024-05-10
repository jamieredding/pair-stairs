import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/components/IdToggleButtonGroup";
import {useEffect, useState} from "react";
import {ArrowForward} from "@mui/icons-material";
import ButtonRow from "@/components/ButtonRow";
import useDeveloperInfos from "@/hooks/developers/useDeveloperInfos";
import Loading from "@/components/Loading";
import Error from "@/components/Error";

interface ChooseDeveloperFormProps {
    savedDeveloperIds?: number[],
    setSavedDeveloperIds: (newDeveloperIds: number[]) => any,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseDevelopersForm({
                                                 savedDeveloperIds,
                                                 setSavedDeveloperIds,
                                                 updateForm
                                             }: ChooseDeveloperFormProps) {
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