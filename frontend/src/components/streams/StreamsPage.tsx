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
import {PostAdd} from "@mui/icons-material";
import {useState} from "react";
import useStreamInfos from "../../hooks/streams/useStreamInfos.ts";
import ButtonRow from "../ButtonRow.tsx";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import AddNewStreamForm from "./AddNewStreamForm.tsx";
import {sorted} from "../../utils/displayUtils.ts";
import usePatchStream from "../../hooks/streams/usePatchStream.ts";
import useRefreshStreamInfos from "../../hooks/streams/useRefreshStreamInfos.ts";
import type StreamInfoDto from "../../domain/StreamInfoDto.ts";

export default function StreamsPage() {
    const {allStreams, isError, isLoading} = useStreamInfos();
    const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false)
    const allStreamsSorted = allStreams && sorted(allStreams)

    return (
        <Stack gap={1}>
            <Typography variant="h4">Streams</Typography>
            <ButtonRow>
                <Button variant="outlined" onClick={() => setIsDialogOpen(true)}>
                    <PostAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New stream
                </Button>
            </ButtonRow>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allStreamsSorted && <StreamsList allStreams={allStreamsSorted} /> }
            <AddNewStreamDialog open={isDialogOpen} onClose={() => setIsDialogOpen(false)}/>
        </Stack>
    )
}

interface StreamsListProps {
    allStreams: StreamInfoDto[];
}

function StreamsList({allStreams}: StreamsListProps) {
    const activeStreams = allStreams.filter(s => !s.archived)
    const archivedStreams = allStreams.filter(s => s.archived)
    const {trigger} = usePatchStream();
    const {refresh} = useRefreshStreamInfos()

    function handlePatchStream(existingStream: StreamInfoDto, archived: boolean) {
        trigger({id: existingStream.id, data: {archived: archived}})
            .then(() => refresh({...existingStream, archived: archived}))
    }

    return <>
        <List>
            {activeStreams.map(stream =>
                <ListItem key={stream.id}>
                    <ListItemText primary={stream.displayName}/>
                    <ListItemButton onClick={() => handlePatchStream(stream, true)}>archive</ListItemButton>
                </ListItem>
            )}
        </List>
        {archivedStreams.length > 0 &&
            <>
                <Typography variant="h4">Archived</Typography>
                {archivedStreams.map(stream =>
                    <ListItem key={stream.id}>
                        <ListItemText primary={stream.displayName}/>
                        <ListItemButton
                            onClick={() => handlePatchStream(stream, false)}>unarchive</ListItemButton>
                    </ListItem>
                )}
            </>
        }
    </>
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