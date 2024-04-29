import {Button, Stack, TextField} from "@mui/material";

interface AddNewDeveloperProps {
    onSubmit: () => void
}

export default function AddNewDeveloper({onSubmit}: AddNewDeveloperProps) {
    return (
        <Stack gap={1}>
            <TextField id="new-developer-name" label="Name" variant="outlined"/>
            <Button variant="contained" sx={({alignSelf: "start"})} onClick={onSubmit}>Save</Button>
        </Stack>
    )
}