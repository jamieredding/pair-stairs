import {Stack, TextField} from "@mui/material";
import SaveButton from "@/components/SaveButton";
import {useState} from "react";
import ButtonRow from "@/components/ButtonRow";
import useRefreshStreamInfos from "@/hooks/streams/useRefreshStreamInfos";
import useAddStream from "@/hooks/streams/useAddStream";

interface AddNewStreamFormProps {
    onSubmit: () => void
}

export default function AddNewStreamForm({onSubmit}: AddNewStreamFormProps) {
    const [name, setName] = useState<string>("");
    const {trigger} = useAddStream()
    const {refresh} = useRefreshStreamInfos()

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