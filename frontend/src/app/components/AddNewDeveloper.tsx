import {Stack, TextField} from "@mui/material";
import {useState} from "react";
import SaveButton from "@/app/components/SaveButton";

interface AddNewDeveloperProps {
    onSubmit: () => void
}

export default function AddNewDeveloper({onSubmit}: AddNewDeveloperProps) {
    const [name, setName] = useState<string>();

    return (
        <Stack gap={1}>
            <TextField label="Name" variant="outlined" value={name}
                       onChange={(e) => setName(e.target.value)}/>
            <SaveButton disabled={name === undefined || name.length === 0} onClick={onSubmit}/>
        </Stack>
    )
}