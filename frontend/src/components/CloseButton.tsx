import {Button} from "@mui/material";
import {Close} from "@mui/icons-material";

interface CloseButtonProps {
    onClick: () => void;
    sx?: any;

}

export default function CloseButton({onClick, sx={}}: CloseButtonProps) {
    return (
        <Button sx={sx} onClick={onClick}>
            <Close/>
        </Button>
    )
}