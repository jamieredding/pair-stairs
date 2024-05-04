import {createTheme} from "@mui/material";

export const theme = createTheme({
    components: {
        // todo: remove once the issue is addressed: https://github.com/mui/material-ui/issues/31185
        MuiDialogContent: {
            styleOverrides: {
                root: ({theme}) => ({paddingTop: `${theme.spacing(1)} !important`}),
            },
        },
    }
});

