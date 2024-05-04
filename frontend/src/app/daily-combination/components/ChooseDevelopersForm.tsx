import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/components/IdToggleButtonGroup";
import {useState} from "react";
import {ArrowForward} from "@mui/icons-material";
import ButtonRow from "@/components/ButtonRow";
import {useGetDeveloperInfos} from "@/infrastructure/DeveloperClient";
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";

interface ChooseDeveloperFormProps {
    savedDeveloperIds?: number[],
    setSavedDeveloperIds: (newDeveloperIds: number[]) => any,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseDevelopersForm({savedDeveloperIds, setSavedDeveloperIds, updateForm}: ChooseDeveloperFormProps) {
    const {allDevelopers, isError, isLoading} = useGetDeveloperInfos();

    return (
        <>
            {isError &&
                <p>failed to load developers...</p>
            }
            {isLoading &&
                <p>loading developers...</p>
            }
            {allDevelopers && <LoadedMode allDevelopers={allDevelopers} savedDeveloperIds={savedDeveloperIds}
                                          setSavedDeveloperIds={setSavedDeveloperIds} updateForm={updateForm}/>}
        </>
    )
}

interface LoadedModeProps extends ChooseDeveloperFormProps {
    allDevelopers: DeveloperInfoDto[]
}

function LoadedMode({allDevelopers, savedDeveloperIds, setSavedDeveloperIds, updateForm}: LoadedModeProps) {
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>(() => savedDeveloperIds || allDevelopers.map(dev => dev.id));

    function progressForm() {
        setSavedDeveloperIds(selectedDeveloperIds);
        updateForm(prevState => prevState + 1)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">Who is in today?</Typography>
            <IdToggleButtonGroup allItems={allDevelopers} selectedIds={selectedDeveloperIds}
                                 setSelectedIds={setSelectedDeveloperIds}/>
            <Divider/>
            <ButtonRow>
                <Button variant="contained"
                        onClick={progressForm}
                >
                    Next
                    <ArrowForward sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
        </Stack>
    )
}