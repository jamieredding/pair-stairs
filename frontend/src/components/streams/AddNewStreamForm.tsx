import {AlertTitle, Stack, TextField} from "@mui/material";
import {useState} from "react";
import ButtonRow from "../ButtonRow.tsx";
import SaveButton from "../SaveButton.tsx";
import useAddStream from "../../hooks/streams/useAddStream.ts";
import useRefreshStreamInfos from "../../hooks/streams/useRefreshStreamInfos.ts";
import ErrorSnackbar from "../ErrorSnackbar.tsx";

interface AddNewStreamFormProps {
    onSubmit: () => void
}

export default function AddNewStreamForm({onSubmit}: AddNewStreamFormProps) {
    const [name, setName] = useState<string>("");
    const {trigger, isLoading: loadingAdd, isError: addError} = useAddStream()
    const {refresh} = useRefreshStreamInfos()

    function handleSubmit() {
        const newName = name as string;
        trigger({name: newName})
            .then(async () => {
                await refresh({id: -1, displayName: newName, archived: false})
                onSubmit();
            })
    }

    return (
        <Stack gap={1}>
            <TextField label="Name" variant="outlined" value={name}
                       onChange={(e) => setName(e.target.value)}/>
            <ButtonRow>
                <SaveButton disabled={name.length === 0} loading={loadingAdd} onClick={handleSubmit}/>
            </ButtonRow>
            <ErrorSnackbar error={addError} alertContent={(errorCode: string) =>
                <>
                    <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                    Error while trying to add, try again.
                </>
            } />
        </Stack>
    )
}