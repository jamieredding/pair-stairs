import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import {ArrowBack, ArrowForward, PersonAdd} from "@mui/icons-material";
import AddNewStream from "@/app/components/AddNewStream";

const streams = [
    {id: 1, displayName: "stream-a"},
    {id: 2, displayName: "stream-b"},
    {id: 3, displayName: "stream-c"},
]

interface ChooseStreamsProps {
    savedStreamIds: number[],
    setSavedStreamIds: (_: (prev: number[]) => number[]) => any,
    updateForm: (value: (((prevState: number) => number) | number)) => void
}

export default function ChooseStreams({savedStreamIds, setSavedStreamIds, updateForm}: ChooseStreamsProps) {
    const allStreams = streams;
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>(() => allStreams.map(dev => dev.id));
    const [addingNewStream, setAddingNewStream] = useState<boolean>(false)

    return (
        <Stack gap={1}>
            <Typography variant="h4">What streams will be worked on?</Typography>
            <IdToggleButtonGroup allItems={allStreams} selectedIds={selectedStreamIds}
                                 setSelectedIds={setSelectedStreamIds}/>
            <Divider/>
            <Stack direction="row" gap={1}>
                <Button variant="outlined" onClick={() => updateForm(prevState => prevState - 1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>

                <Button variant="outlined" onClick={() => setAddingNewStream(!addingNewStream)}>
                    <PersonAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New stream
                </Button>

                <Button variant="contained">
                    Next
                    <ArrowForward sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </Stack>
            {addingNewStream &&
                <Stack gap={1}>
                    <Divider/>
                    <AddNewStream onSubmit={() => setAddingNewStream(!addingNewStream)}/>
                </Stack>
            }
        </Stack>
    );
}