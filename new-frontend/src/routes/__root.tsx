import {createRootRoute, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import RootContext from "../components/RootContext.tsx";
import {CssBaseline} from "@mui/material";
import Navbar from "../components/Navbar.tsx";
import Toolbar from "@mui/material/Toolbar";

export const Route = createRootRoute({
    component: () => (
        <RootContext>
            <CssBaseline/>
            <Navbar/>
            {/*To pad the navbar above, see: https://github.com/mui/material-ui/issues/16844#issuecomment-517205129 */}
            <Toolbar sx={{mb: 1}}/>
            <Outlet/>
            <TanStackRouterDevtools/>
        </RootContext>
    ),
})