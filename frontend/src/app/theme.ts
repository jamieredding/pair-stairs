import {createTheme} from "@mui/material";

export const theme = createTheme({
    components: {
        // todo: remove once the issue is addressed: https://github.com/mui/material-ui/issues/31185
        MuiDialogContent: {
            styleOverrides: {
                root: ({theme}) => ({paddingTop: `${theme.spacing(1)} !important`}),
            },
        },
        MuiTypography: {
            defaultProps: {
                variantMapping: {
                    h4: 'h2',
                    h5: 'h3',
                    h6: 'h4',
                },
            },
        }
    },
    palette: {
        mode: 'dark',
        primary: {
            main: '#3f51b5',
        },
        secondary: {
            main: '#f50057',
        },
    },
});

