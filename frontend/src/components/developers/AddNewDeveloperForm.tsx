import {AlertTitle, Stack, TextField} from "@mui/material";
import {useState} from "react";
import SaveButton from "../SaveButton.tsx";
import ButtonRow from "../ButtonRow.tsx";
import useAddDeveloper from "../../hooks/developers/useAddDeveloper.ts";
import useRefreshDeveloperInfos from "../../hooks/developers/useRefreshDeveloperInfos.ts";
import ErrorSnackbar from "../ErrorSnackbar.tsx";

interface AddNewDeveloperFormProps {
    onSubmit: () => void
}

export default function AddNewDeveloperForm({onSubmit}: AddNewDeveloperFormProps) {
    const [name, setName] = useState<string>("");
    const {trigger, isLoading: loadingAdd, isError: addError} = useAddDeveloper()
    const {refresh} = useRefreshDeveloperInfos()

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