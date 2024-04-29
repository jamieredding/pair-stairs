import {Button, Divider, Stack, Typography} from "@mui/material";
import {ArrowBack, Save} from "@mui/icons-material";

interface ChooseCombinationProps {
    developerIds: number[],
    streamIds: number[],
    updateForm: (value: (((prevState: number) => number) | number)) => void
}

export default function ChooseCombination({developerIds, streamIds, updateForm}: ChooseCombinationProps) {
    function progressForm(direction: number) {
        updateForm(prevState => prevState + direction)
    }

    return (
        <Stack gap={1}>
            <Typography variant="h4">Possible combinations</Typography>
            <Divider/>
            <Stack direction="row" gap={1}>
                <Button variant="outlined" onClick={() => progressForm(-1)}>
                    <ArrowBack sx={({marginRight: (theme) => theme.spacing(1)})}/>
                    Back
                </Button>

                <Button variant="contained">
                    Save
                    <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
                </Button>
            </Stack>
        </Stack>
    );

}