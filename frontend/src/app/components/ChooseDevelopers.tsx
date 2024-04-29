import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import {PersonAdd} from "@mui/icons-material";

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
    savedDeveloperIds: number[];
    setSavedDeveloperIds: (_: (prev: number[]) => number[]) => any;
}

export default function ChooseDevelopers({savedDeveloperIds, setSavedDeveloperIds}: ChooseDeveloperProps) {
    const allDevelopers = developers;
    const [selectedDeveloperIds, setSelectedDeveloperIds] = useState<number[]>(() => allDevelopers.map(dev => dev.id));

    return (
        <Stack gap={1}>
            <Typography variant="h4">Who is in today?</Typography>
            <IdToggleButtonGroup allItems={allDevelopers} selectedIds={selectedDeveloperIds}
                                 setSelectedIds={setSelectedDeveloperIds}/>
            <Divider/>
            <Stack direction="row" gap={1}>

            <Button variant="outlined"><PersonAdd sx={{marginRight: (theme) => theme.spacing(1)}}/> New developer</Button>
            <Button variant="contained">Next</Button>
            </Stack>
        </Stack>
    )
}