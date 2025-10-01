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
import {PersonAdd} from "@mui/icons-material";
import {useState} from "react";

import ButtonRow from "../ButtonRow.tsx";
import useDeveloperInfos from "../../hooks/developers/useDeveloperInfos.ts";
import AddNewDeveloperForm from "./AddNewDeveloperForm.tsx";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import {sorted} from "../../utils/displayUtils.ts";
import type DeveloperInfoDto from "../../domain/DeveloperInfoDto.ts";
import usePatchDeveloper from "../../hooks/developers/usePatchDeveloper.ts";
import useRefreshDeveloperInfos from "../../hooks/developers/useRefreshDeveloperInfos.ts";

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
    const activeDevelopers = allDevelopers.filter(d => !d.archived)
    const archivedDevelopers = allDevelopers.filter(d => d.archived)
    const {trigger} = usePatchDeveloper();
    const {refresh} = useRefreshDeveloperInfos()

    function handlePatchDeveloper(existingDeveloper: DeveloperInfoDto, archived: boolean) {
        trigger({id: existingDeveloper.id, data: {archived: archived}})
            .then(() => refresh({...existingDeveloper, archived: archived}))
    }

    return <>
        <List>
            {activeDevelopers.map(developer =>
                <ListItem key={developer.id}>
                    <ListItemText primary={developer.displayName}/>
                    <ListItemButton onClick={() => handlePatchDeveloper(developer, true)}>archive</ListItemButton>
                </ListItem>
            )}
        </List>
        {archivedDevelopers.length > 0 &&
            <>
                <Typography variant="h4">Archived</Typography>
                {archivedDevelopers.map(developer =>
                    <ListItem key={developer.id}>
                        <ListItemText primary={developer.displayName}/>
                        <ListItemButton
                            onClick={() => handlePatchDeveloper(developer, false)}>unarchive</ListItemButton>
                    </ListItem>
                )}
            </>
        }
    </>
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