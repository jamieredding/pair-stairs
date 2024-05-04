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
import DeveloperInfoDto from "@/domain/DeveloperInfoDto";
import ButtonRow from "@/components/ButtonRow";
import {PersonAdd} from "@mui/icons-material";
import {useState} from "react";
import AddNewDeveloper from "@/components/AddNewDeveloper";
import {useGetDeveloperInfos} from "@/infrastructure/DeveloperClient";

export default function DevelopersPage() {
    const {allDevelopers, isError, isLoading} = useGetDeveloperInfos();

    return (
        <>
            {isError &&
                <p>failed to load developers...</p>
            }
            {isLoading &&
                <p>loading developers...</p>
            }
            {allDevelopers && <LoadedMode allDevelopers={allDevelopers}/>}
        </>
    )
}

interface LoadedModeProps {
    allDevelopers: DeveloperInfoDto[]
}

function LoadedMode({allDevelopers}: LoadedModeProps) {
    const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false)

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
            <List>
                {allDevelopers.map(developer =>
                    <ListItem key={developer.id}>
                        <ListItemButton>
                            <ListItemText primary={developer.displayName}/>
                        </ListItemButton>
                    </ListItem>
                )}
            </List>
            <AddNewDeveloperDialog open={isDialogOpen} onClose={() => setIsDialogOpen(false)}/>
        </Stack>
    )
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
                <AddNewDeveloper onSubmit={onClose}/>
            </DialogContent>
        </Dialog>
    )
}