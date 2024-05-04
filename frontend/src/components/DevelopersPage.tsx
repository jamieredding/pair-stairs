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

const developers: DeveloperInfoDto[] = [
    {id: 1, displayName: "dev-1"},
    {id: 2, displayName: "dev-2"},
    {id: 3, displayName: "dev-3"},
    {id: 4, displayName: "dev-4"},
    {id: 5, displayName: "dev-5"},
    {id: 6, displayName: "dev-6"},
    {id: 7, displayName: "Jamie with a long long name!"},
]

export default function DevelopersPage() {
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
                {developers.map(developer =>
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