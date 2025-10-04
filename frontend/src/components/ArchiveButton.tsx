import {Archive} from "@mui/icons-material";
import {Button} from "@mui/material";

export interface ArchiveButtonProps {
    onClick?: () => void;
}

export default function ArchiveButton({onClick}: ArchiveButtonProps) {
    return <Button variant="outlined" onClick={onClick}>
        <Archive sx={{marginRight: (theme) => theme.spacing(1)}}/>
        Archive
    </Button>
}