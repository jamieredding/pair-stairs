import {Unarchive} from "@mui/icons-material";
import {Button} from "@mui/material";

export interface UnarchiveButtonProps {
    onClick?: () => void;
}

export default function UnarchiveButton({onClick}: UnarchiveButtonProps) {
    return <Button variant="outlined" onClick={onClick}>
        <Unarchive sx={{marginRight: (theme) => theme.spacing(1)}}/>
        Unarchive
    </Button>
}