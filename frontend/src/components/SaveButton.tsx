import {Button} from "@mui/material";
import {Save} from "@mui/icons-material";

interface SaveButtonProps {
    disabled?: boolean,
    onClick?: () => void
}

export default function SaveButton({disabled = false, onClick = () => undefined}: SaveButtonProps) {
    return (
        <Button variant="contained" disabled={disabled} onClick={onClick}>
            Save
            <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
        </Button>
    )
}