import {
    AlertTitle,
    Button,
    Collapse,
    Dialog,
    DialogContent,
    DialogTitle,
    List,
    ListItem,
    ListItemText,
    Stack,
    Typography
} from "@mui/material";
import {PostAdd} from "@mui/icons-material";
import {type ReactElement, useMemo, useState} from "react";
import useStreamInfos from "../../hooks/streams/useStreamInfos.ts";
import ButtonRow from "../ButtonRow.tsx";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import AddNewStreamForm from "./AddNewStreamForm.tsx";
import {sorted} from "../../utils/displayUtils.ts";
import usePatchStream from "../../hooks/streams/usePatchStream.ts";
import useRefreshStreamInfos from "../../hooks/streams/useRefreshStreamInfos.ts";
import type StreamInfoDto from "../../domain/StreamInfoDto.ts";
import ArchiveButton from "../ArchiveButton.tsx";
import UnarchiveButton from "../UnarchiveButton.tsx";
import ErrorSnackbar from "../ErrorSnackbar.tsx";

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
            {allStreamsSorted && <StreamsList allStreams={allStreamsSorted}/>}
            <AddNewStreamDialog open={isDialogOpen} onClose={() => setIsDialogOpen(false)}/>
        </Stack>
    )
}

interface StreamsListProps {
    allStreams: StreamInfoDto[];
}

function StreamsList({allStreams}: StreamsListProps) {
    const [archiveOpen, setArchiveOpen] = useState(false);
    const activeStreams = useMemo(() => allStreams.filter(d => !d.archived), [allStreams])
    const archivedStreams = useMemo(() => allStreams.filter(d => d.archived), [allStreams])
    const {trigger, isError} = usePatchStream();
    const {refresh} = useRefreshStreamInfos()

    function handlePatchStream(existingStream: StreamInfoDto, archived: boolean) {
        trigger({id: existingStream.id, data: {archived: archived}})
            .then(() => refresh({...existingStream, archived: archived}))
            .then(() => {
                if (archived) {
                    setArchiveOpen(true)
                }
            })
    }

    return <>
        <List>
            {activeStreams.map(stream =>
                <ListItem key={stream.id}>
                    <Stack direction="row" gap={4}>
                        <ListItemText primary={stream.displayName}/>
                        <ArchiveButton onClick={() => handlePatchStream(stream, true)}/>
                    </Stack>
                </ListItem>
            )}
        </List>
        <Collapse in={archivedStreams.length > 0}>
            <Stack>
                <Stack direction="row">
                    <Typography variant="h5" sx={{marginRight: "auto"}}>Archived streams</Typography>
                    {archiveOpen
                        ? <Button onClick={() => setArchiveOpen(false)}>Hide</Button>
                        : <Button onClick={() => setArchiveOpen(true)}>Show</Button>}
                </Stack>
                <Collapse in={archiveOpen}>
                    {archivedStreams.map(stream =>
                        <ListItem key={stream.id}>
                            <Stack direction="row" gap={4}>
                                <ListItemText primary={stream.displayName}/>
                                <UnarchiveButton onClick={() => handlePatchStream(stream, false)}/>
                            </Stack>
                        </ListItem>
                    )}
                </Collapse>
            </Stack>
        </Collapse>
        <ErrorSnackbar error={isError} alertContent={alertContent} />
    </>
}

function alertContent(errorCode: string): ReactElement {
    switch (errorCode) {
        default:
            return <>
                <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                Unable to update stream.
            </>
    }
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