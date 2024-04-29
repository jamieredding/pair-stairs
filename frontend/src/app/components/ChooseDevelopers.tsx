import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import {ArrowForward, PersonAdd} from "@mui/icons-material";
import AddNewDeveloper from "@/app/components/AddNewDeveloper";

const developers = [
    {id: 1, displayName: "dev-1"},
    {id: 2, displayName: "dev-2"},
    {id: 3, displayName: "dev-3"},
    {id: 4, displayName: "dev-4"},
    {id: 5, displayName: "dev-5"},
    {id: 6, displayName: "dev-6"},
    {id: 7, displayName: "Jamie with a long long name!"},
]

interface ChooseDeveloperProps {
    savedDeveloperIds: number[],
    setSavedDeveloperIds: (_: (prev: number[]) => number[]) => any,
    updateForm: (value: ((prevState: number) => number)) => void
}

export default function ChooseDevelopers({savedDeveloperIds, setSavedDeveloperIds, updateForm}: ChooseDeveloperProps) {
    const allDevelopers = developers;
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>(() => allDevelopers.map(dev => dev.id));
    const [addingNewDeveloper, setAddingNewDeveloper] = useState<boolean>(false)

    return (
        <Stack gap={1}>
            <Typography variant="h4">Who is in today?</Typography>
            <IdToggleButtonGroup allItems={allDevelopers} selectedIds={selectedDeveloperIds}
                                 setSelectedIds={setSelectedDeveloperIds}/>
            <Divider/>
            <Stack direction="row" gap={1}>

                <Button variant="outlined" onClick={() => setAddingNewDeveloper(!addingNewDeveloper)}>
                    <PersonAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New developer
                </Button>

                <Button variant="contained"
                        onClick={() => updateForm(prevState => prevState + 1)}
                >
                    Next
                    <ArrowForward sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </Stack>
            {addingNewDeveloper &&
                <Stack gap={1}>
                    <Divider/>
                    <AddNewDeveloper onSubmit={() => setAddingNewDeveloper(!addingNewDeveloper)}/>
                </Stack>
            }
        </Stack>
    )
}