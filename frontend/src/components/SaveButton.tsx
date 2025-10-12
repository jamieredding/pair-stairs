import {Button, CircularProgress} from "@mui/material";
import {Save} from "@mui/icons-material";

interface SaveButtonProps {
    disabled?: boolean,
    onClick?: () => void,
    text?: string,
    loading?: boolean,
}

export default function SaveButton({disabled = false, onClick = () => undefined, text = "Save", loading = false}: SaveButtonProps) {
    return (
        <Button variant="contained" disabled={disabled || loading} onClick={onClick}>
            {text}
            {
                loading
                    ? <CircularProgress size={20} sx={({marginLeft: (theme) => theme.spacing(1)})} />
                    : <Save sx={({marginLeft: (theme) => theme.spacing(1)})}/>
            }
        </Button>
    )
}