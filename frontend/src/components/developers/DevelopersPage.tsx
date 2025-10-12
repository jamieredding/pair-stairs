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
import {PersonAdd} from "@mui/icons-material";
import {type ReactElement, useMemo, useState} from "react";

import ButtonRow from "../ButtonRow.tsx";
import useDeveloperInfos from "../../hooks/developers/useDeveloperInfos.ts";
import AddNewDeveloperForm from "./AddNewDeveloperForm.tsx";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import {sorted} from "../../utils/displayUtils.ts";
import type DeveloperInfoDto from "../../domain/DeveloperInfoDto.ts";
import usePatchDeveloper from "../../hooks/developers/usePatchDeveloper.ts";
import useRefreshDeveloperInfos from "../../hooks/developers/useRefreshDeveloperInfos.ts";
import ArchiveButton from "../ArchiveButton.tsx";
import UnarchiveButton from "../UnarchiveButton.tsx";
import ErrorSnackbar from "../ErrorSnackbar.tsx";

export default function DevelopersPage() {
    const {allDevelopers, isError, isLoading} = useDeveloperInfos();
    const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false)
    const allDevelopersSorted = allDevelopers && sorted(allDevelopers)

    return (
        <Stack gap={1}>
            <Typography variant="h4">Developers</Typography>
            <ButtonRow>
                <Button variant="outlined"
                        onClick={() => setIsDialogOpen(true)}
                >
                    <PersonAdd sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    New developer
                </Button>
            </ButtonRow>
            {isLoading && <Loading/>}
            {isError && <Error/>}
            {allDevelopersSorted && <DevelopersList allDevelopers={allDevelopersSorted}/>}
            <AddNewDeveloperDialog open={isDialogOpen} onClose={() => setIsDialogOpen(false)}/>
        </Stack>
    )
}

interface DevelopersListProps {
    allDevelopers: DeveloperInfoDto[];
}

function DevelopersList({allDevelopers}: DevelopersListProps) {
    const [archiveOpen, setArchiveOpen] = useState(false);
    const activeDevelopers = useMemo(() => allDevelopers.filter(d => !d.archived), [allDevelopers])
    const archivedDevelopers = useMemo(() => allDevelopers.filter(d => d.archived), [allDevelopers])
    const [idBeingUpdated, setIdBeingUpdated] = useState<number>()
    const {trigger, isError, isLoading} = usePatchDeveloper();
    const {refresh} = useRefreshDeveloperInfos()

    function handlePatchDeveloper(existingDeveloper: DeveloperInfoDto, archived: boolean) {
        setIdBeingUpdated(existingDeveloper.id)
        trigger({id: existingDeveloper.id, data: {archived: archived}})
            .then(() => setIdBeingUpdated(undefined))
            .then(() => refresh({...existingDeveloper, archived: archived}))
            .then(() => {
                if (archived) {
                    setArchiveOpen(true)
                }
            })
    }

    return <>
        <List>
            {activeDevelopers.map(developer =>
                <ListItem key={developer.id}>
                    <Stack direction="row" gap={4}>
                        <ListItemText primary={developer.displayName}/>
                        <ArchiveButton disabled={isLoading} loading={idBeingUpdated === developer.id && isLoading} onClick={() => handlePatchDeveloper(developer, true)}/>
                    </Stack>
                </ListItem>
            )}
        </List>
        <Collapse in={archivedDevelopers.length > 0}>
            <Stack>
                <Stack direction="row">
                    <Typography variant="h5" sx={{marginRight: "auto"}}>Archived developers</Typography>
                    {archiveOpen
                        ? <Button onClick={() => setArchiveOpen(false)}>Hide</Button>
                        : <Button onClick={() => setArchiveOpen(true)}>Show</Button>}
                </Stack>
                <Collapse in={archiveOpen}>
                    {archivedDevelopers.map(developer =>
                        <ListItem key={developer.id}>
                            <Stack direction="row" gap={4}>
                                <ListItemText primary={developer.displayName}/>
                                <UnarchiveButton onClick={() => handlePatchDeveloper(developer, false)}/>
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
                Unable to update developer.
            </>
    }
}

interface AddNewDeveloperDialogProps {
    open: boolean,
    onClose: () => void
}

function AddNewDeveloperDialog({open, onClose}: AddNewDeveloperDialogProps) {
    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>Add new developer</DialogTitle>
            <DialogContent>
                <AddNewDeveloperForm onSubmit={onClose}/>
            </DialogContent>
        </Dialog>
    )
}