import {Archive} from "@mui/icons-material";
import {Button, CircularProgress} from "@mui/material";

export interface ArchiveButtonProps {
    disabled?: boolean,
    onClick?: () => void,
    loading?: boolean,
}

export default function ArchiveButton({disabled = false, onClick, loading = false}: ArchiveButtonProps) {
    return <Button variant="outlined" onClick={onClick} disabled={disabled || loading}>
        {
            loading
                ? <CircularProgress size={20} sx={({marginRight: (theme) => theme.spacing(1)})}/>
                : <Archive sx={{marginRight: (theme) => theme.spacing(1)}}/>
        }
        Archive
    </Button>
}