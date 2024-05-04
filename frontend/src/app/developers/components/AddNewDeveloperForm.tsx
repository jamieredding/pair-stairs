import {Stack, TextField} from "@mui/material";
import {useState} from "react";
import SaveButton from "@/components/SaveButton";
import ButtonRow from "@/components/ButtonRow";
import useRefreshDeveloperInfos from "@/hooks/developers/useRefreshDeveloperInfos";
import useAddDeveloper from "@/hooks/developers/useAddDeveloper";

interface AddNewDeveloperFormProps {
    onSubmit: () => void
}

export default function AddNewDeveloperForm({onSubmit}: AddNewDeveloperFormProps) {
    const [name, setName] = useState<string>("");
    const {trigger} = useAddDeveloper()
    const {refresh} = useRefreshDeveloperInfos()

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