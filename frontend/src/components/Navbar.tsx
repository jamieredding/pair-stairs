import Toolbar from "@mui/material/Toolbar";
import AppBar from "@mui/material/AppBar";
import {Link, Stack} from "@mui/material";
import {Fragment} from "react";

interface NavItem {
    displayText: string;
    link: string;
}

const navItems: NavItem[] = [
    {displayText: "Daily combination", link: "/daily-combination"},
    {displayText: "Manual combination", link: "/manual-combination"},
    {displayText: "Developers", link: "/developers"},
    {displayText: "Streams", link: "#"},
]

export default function Navbar() {
    return (
        <AppBar>
            <Toolbar>
                <Stack direction="row" gap={2} alignItems="center">
                    <Link variant="h4" href="/frontend/public" sx={{color: '#fff'}} underline="none" pr={1}>
                        pair-stairs
                    </Link>
                    {navItems.map((item, index) => (
                        <Fragment key={index}>
                            <Link
                                key={item.link}
                                sx={{color: '#fff'}}
                                href={item.link}>
                                {item.displayText}
                            </Link>
                        </Fragment>
                    ))}
                </Stack>
            </Toolbar>
        </AppBar>
    )
}