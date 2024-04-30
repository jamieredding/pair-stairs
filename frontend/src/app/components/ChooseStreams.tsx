import {Button, Divider, Stack, Typography} from "@mui/material";
import IdToggleButtonGroup from "@/app/components/IdToggleButtonGroup";
import {useState} from "react";
import {ArrowBack, List, PostAdd} from "@mui/icons-material";
import AddNewStream from "@/app/components/AddNewStream";
import ButtonRow from "@/app/components/ButtonRow";

const streams = [
    {id: 1, displayName: "stream-a"},
    {id: 2, displayName: "stream-b"},
    {id: 3, displayName: "stream-c"},
]

interface ChooseStreamsProps {
    savedStreamIds?: number[],
    setSavedStreamIds: (newStreamIds: number[]) => any,
    updateForm: (value: (((prevState: number) => number) | number)) => void
}

export default function ChooseStreams({savedStreamIds, setSavedStreamIds, updateForm}: ChooseStreamsProps) {
    const allStreams = streams;
    const [selectedStreamIds, setSelectedStreamIds] = useState<number[]>(() => savedStreamIds || allStreams.map(dev => dev.id));
    const [addingNewStream, setAddingNewStream] = useState<boolean>(false)

    function progressForm(direction: number) {
        setSavedStreamIds(selectedStreamIds);
        updateForm(prevState => prevState + direction)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">What streams will be worked on?</Typography>
            <IdToggleButtonGroup allItems={allStreams} selectedIds={selectedStreamIds}
                                 setSelectedIds={setSelectedStreamIds}/>
            <Divider/>
            <ButtonRow>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>

                <Button variant="outlined" onClick={() => setAddingNewStream(!addingNewStream)}>
                    <PostAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New stream
                </Button>

                <Button variant="contained" onClick={() => progressForm(1)}>
                    See combinations
                    <List sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </ButtonRow>
            {addingNewStream &&
                <Stack gap={1}>
                    <Divider/>
                    <AddNewStream onSubmit={() => setAddingNewStream(!addingNewStream)}/>
                </Stack>
            }
        </Stack>
    );
}