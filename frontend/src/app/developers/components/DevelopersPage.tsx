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
import {PersonAdd} from "@mui/icons-material";
import {useState} from "react";
import AddNewDeveloperForm from "@/app/developers/components/AddNewDeveloperForm";

import useDeveloperInfos from "@/hooks/developers/useDeveloperInfos";
import Loading from "@/components/Loading";
import Error from "@/components/Error";

export default function DevelopersPage() {
    const {allDevelopers, isError, isLoading} = useDeveloperInfos();
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
                {isLoading && <Loading/>}
                {isError && <Error/>}
                {allDevelopers &&
                    allDevelopers.map(developer =>
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
                <AddNewDeveloperForm onSubmit={onClose}/>
            </DialogContent>
        </Dialog>
    )
}