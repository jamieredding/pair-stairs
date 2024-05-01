import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import {ArrowForward, PersonAdd} from "@mui/icons-material";
import AddNewDeveloper from "@/app/components/AddNewDeveloper";
import ButtonRow from "@/app/components/ButtonRow";
import {useGetDeveloperInfos} from "@/app/infrastructure/DeveloperClient";
import DeveloperInfoDto from "@/app/domain/DeveloperInfoDto";

interface ChooseDeveloperProps {
    savedDeveloperIds?: number[],
    setSavedDeveloperIds: (newDeveloperIds: number[]) => any,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseDevelopers({savedDeveloperIds, setSavedDeveloperIds, updateForm}: ChooseDeveloperProps) {
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

interface LoadedModeProps extends ChooseDeveloperProps {
    allDevelopers: DeveloperInfoDto[]
}

function LoadedMode({allDevelopers, savedDeveloperIds, setSavedDeveloperIds, updateForm}: LoadedModeProps) {
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>(() => savedDeveloperIds || allDevelopers.map(dev => dev.id));
    const [addingNewDeveloper, setAddingNewDeveloper] = useState<boolean>(false)

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
                <Button variant="outlined" onClick={() => setAddingNewDeveloper(!addingNewDeveloper)}>
                    <PersonAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New developer
                </Button>

                <Button variant="contained"
                        onClick={progressForm}
                >
                    Next
                    <ArrowForward sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
            {addingNewDeveloper &&
                <Stack gap={1}>
                    <Divider/>
                    <AddNewDeveloper onSubmit={() => setAddingNewDeveloper(!addingNewDeveloper)}/>
                </Stack>
            }
        </Stack>
    )
}