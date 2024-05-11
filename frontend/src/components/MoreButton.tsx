import {Button} from "@mui/material";
import {ArrowDownward} from "@mui/icons-material";

interface MoreButtonProps {
    onClick?: () => void,
    disabled?: boolean
}

export default function MoreButton({onClick, disabled}: MoreButtonProps) {
    return (
        <Button variant="outlined" onClick={onClick} disabled={disabled}>
            <ArrowDownward sx={({marginRight: (theme) => theme.spacing(1)})}/>
            More
        </Button>
    )
}