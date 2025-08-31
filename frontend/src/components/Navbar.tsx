import Toolbar from "@mui/material/Toolbar";
import AppBar from "@mui/material/AppBar";
import {Fragment} from "react";
import {CustomLink} from "./CustomLink.tsx";
import {Stack} from "@mui/material";
import {logout} from "../infrastructure/LogoutClient.ts";

interface NavItem {
    displayText: string;
    link: string;
}

const navItems: NavItem[] = [
    {displayText: "Developers", link: "/developers"},
    {displayText: "Streams", link: "/streams"},
]

export default function Navbar() {
    const handleLogout = () => {
        logout()
    }

    return (
        <AppBar>
            <Toolbar>
                <Stack direction="row" gap={2} alignItems="center" sx={{flexGrow: 1}}>
                    <CustomLink to="/" variant="h4" sx={{color: '#fff'}} underline="none" pr={1}>
                        pair-stairs
                    </CustomLink>
                    {navItems.map((item, index) => (
                        <Fragment key={index}>
                            <CustomLink
                                to={item.link}
                                key={item.link}
                                sx={{color: '#fff'}}
                            >
                                {item.displayText}
                            </CustomLink>
                        </Fragment>
                    ))}
                </Stack>
                <CustomLink to="." sx={{color: '#fff'}} onClick={handleLogout}>Log out</CustomLink>
            </Toolbar>
        </AppBar>
    )
}