import {Button, Stack, TextField} from "@mui/material";

interface AddNewStreamProps {
    onSubmit: () => void
}

export default function AddNewStream({onSubmit}: AddNewStreamProps) {
    return (
        <Stack gap={1}>
            {/* todo make this controlled */}
            <TextField id="new-stream-name" label="Name" variant="outlined"/>
            <Button variant="contained" sx={({alignSelf: "start"})} onClick={onSubmit}>Save</Button>
        </Stack>
    )
}