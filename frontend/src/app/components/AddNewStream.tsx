import {Stack, TextField} from "@mui/material";
import SaveButton from "@/app/components/SaveButton";
import {useState} from "react";

interface AddNewStreamProps {
    onSubmit: () => void
}

export default function AddNewStream({onSubmit}: AddNewStreamProps) {
    const [name, setName] = useState<string>();

    return (
        <Stack gap={1}>
            <TextField label="Name" variant="outlined" value={name}
                       onChange={(e) => setName(e.target.value)}/>
            <SaveButton disabled={name === undefined || name.length === 0} onClick={onSubmit}/>
        </Stack>
    )
}