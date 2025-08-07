import {createRootRoute, HeadContent, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import RootContext from "../components/RootContext.tsx";
import {CssBaseline} from "@mui/material";
import Navbar from "../components/Navbar.tsx";
import Toolbar from "@mui/material/Toolbar";

export const Route = createRootRoute({
    head: () => ({
        meta: [
            {
                name: "description",
                content: "A tool for helping developers pair"
            },
            {
                title: "Pair Stairs"
            }
        ],
        links: [
            {
                rel: "icon",
                href: "/favicon.ico"
            }
        ]
    }),
    component: () => (
        <>
            <HeadContent/>
            <RootContext>
                <CssBaseline/>
                <Navbar/>
                {/*To pad the navbar above, see: https://github.com/mui/material-ui/issues/16844#issuecomment-517205129 */}
                <Toolbar sx={{mb: 1}}/>
                <Outlet/>
                <TanStackRouterDevtools/>
            </RootContext>
        </>
    ),
})