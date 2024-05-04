"use client"

import {
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    List,
    ListItem,
    ListItemButton,
    ListItemText,
    Stack,
    Typography
} from "@mui/material";
import ButtonRow from "@/components/ButtonRow";
import {PostAdd} from "@mui/icons-material";
import {useState} from "react";
import StreamInfoDto from "@/domain/StreamInfoDto";
import AddNewStreamForm from "@/app/streams/components/AddNewStreamForm";
import useStreamInfos from "@/hooks/streams/useStreamInfos";

export default function StreamsPage() {
    const {allStreams, isError, isLoading} = useStreamInfos();

    return (
        <>
            {isError &&
                <p>failed to load streams...</p>
            }
            {isLoading &&
                <p>loading streams...</p>
            }
            {allStreams && <LoadedMode allStreams={allStreams}/>}
        </>
    )
}

interface LoadedModeProps {
    allStreams: StreamInfoDto[];
}

function LoadedMode({allStreams}: LoadedModeProps) {
    const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false)

    return (
        <Stack gap={1}>
            <Typography variant="h4">Streams</Typography>
            <ButtonRow>
                <Button variant="outlined" onClick={() => setIsDialogOpen(true)}>
                    <PostAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New stream
                </Button>
            </ButtonRow>
            <List>
                {allStreams.map(stream =>
                    <ListItem key={stream.id}>
                        <ListItemButton>
                            <ListItemText primary={stream.displayName}/>
                        </ListItemButton>
                    </ListItem>
                )}
            </List>
            <AddNewStreamDialog open={isDialogOpen} onClose={() => setIsDialogOpen(false)}/>
        </Stack>
    )
}

interface AddNewDialogProps {
    open: boolean,
    onClose: () => void
}

function AddNewStreamDialog({open, onClose}: AddNewDialogProps) {
    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>Add new stream</DialogTitle>
            <DialogContent>
                <AddNewStreamForm onSubmit={onClose}/>
            </DialogContent>
        </Dialog>
    )
}