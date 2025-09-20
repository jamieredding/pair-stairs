import {Button} from "@mui/material";
import {Save} from "@mui/icons-material";

interface SaveButtonProps {
    disabled?: boolean,
    onClick?: () => void,
    text?: string,
}

export default function SaveButton({disabled = false, onClick = () => undefined, text = "Save"}: SaveButtonProps) {
    return (
        <Button variant="contained" disabled={disabled} onClick={onClick}>
            {text}
            <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
        </Button>
    )
}