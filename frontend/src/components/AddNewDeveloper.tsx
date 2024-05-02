import {Stack, TextField} from "@mui/material";
import {useState} from "react";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import {useAddDeveloper, useRefreshGetDeveloperInfo} from "@/infrastructure/DeveloperClient";

interface AddNewDeveloperProps {
    onSubmit: () => void
}

export default function AddNewDeveloper({onSubmit}: AddNewDeveloperProps) {
    const [name, setName] = useState<string>("");
    const {trigger} = useAddDeveloper()
    const {refresh} = useRefreshGetDeveloperInfo()

    function handleSubmit() {
        const newName = name as string;
        trigger({name: newName})
            .then(async () => {
                await refresh({id: -1, displayName: newName})
                onSubmit();
            })
    }

    return (
        <Stack gap={1}>
            <TextField label="Name" variant="outlined" value={name}
                       onChange={(e) => setName(e.target.value)}/>
            <ButtonRow>
                <SaveButton disabled={name.length === 0} onClick={handleSubmit}/>
            </ButtonRow>
        </Stack>
    )
}