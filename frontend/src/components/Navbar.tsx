import Toolbar from "@mui/material/Toolbar";
import AppBar from "@mui/material/AppBar";
import * as React from "react";
import {Fragment, useState} from "react";
import {CustomLink} from "./CustomLink.tsx";
import {AlertTitle, Divider, Link, Menu, MenuItem, Stack, Typography} from "@mui/material";
import {logout} from "../infrastructure/LogoutClient.ts";
import useCurrentUser from "../hooks/currentUser/useCurrentUser.ts";
import {greet} from "../utils/greetingUtils.ts";
import useTeams from "../hooks/teams/useTeams.ts";
import ErrorSnackbar from "./ErrorSnackbar.tsx";
import Loading from "./Loading.tsx";
import FeatureFlag from "./FeatureFlag.tsx";
import {teamsEnabled} from "../utils/featureFlags.ts";

interface NavItem {
    displayText: string;
    link: string;
}

const navItems: NavItem[] = [
    {displayText: "Developers", link: "/developers"},
    {displayText: "Streams", link: "/streams"},
]

export default function Navbar() {
    return (
        <AppBar>
            <Toolbar sx={{gap: 2}}>
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
                    <FeatureFlag on={teamsEnabled}>
                        <TeamMenu/>
                    </FeatureFlag>
                </Stack>
                <FeatureFlag on={teamsEnabled}>
                    <UserSection />
                </FeatureFlag>
            </Toolbar>
        </AppBar>
    )
}

function TeamMenu() {
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)
    const open = Boolean(anchorEl)
    const {allTeams, isError} = useTeams()

    function handleClick(event: React.MouseEvent<HTMLAnchorElement>) {
        setAnchorEl(event.currentTarget)
    }

    function handleClose() {
        setAnchorEl(null)
    }

    return (
        <div>
            <Link
                id="team-menu-button"
                sx={{color: '#fff', cursor: "pointer"}}

                aria-controls={open ? "team-menu" : undefined}
                aria-haspopup="true"
                aria-expanded={open ? "true" : undefined}
                onClick={handleClick}
            >
                Teams
            </Link>
            <Menu
                id="team-menu"
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                slotProps={{
                    list: {
                        "aria-labelledby": "team-menu-button",
                    }
                }}
            >
                {allTeams ?
                    allTeams.length === 0 ?
                        <MenuItem>No teams found</MenuItem>
                        : allTeams.map(team =>
                            <MenuItem key={team.id} onClick={handleClose}>
                                <CustomLink to="/team/$teamName" params={{teamName: team.slug}} underline="none"
                                            sx={{color: '#fff'}}>{team.name}</CustomLink>
                            </MenuItem>
                        )
                    : <MenuItem><Loading/></MenuItem>
                }
                <Divider/>
                <MenuItem onClick={handleClose}>
                    <CustomLink to="/new/team" underline="none" sx={{color: '#fff'}}>
                        New team
                    </CustomLink>
                </MenuItem>
            </Menu>
            <ErrorSnackbar error={isError} alertContent={(errorCode: string) =>
                <>
                    <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                    Error while trying to load teams.
                </>
            }/>
        </div>
    )
}

function UserSection() {
    const {currentUser} = useCurrentUser();
    const handleLogout = () => {
        logout()
    }
    return (
        <>
            {currentUser && <Typography variant="body1">{greet(currentUser.displayName)}</Typography>}
            <Divider orientation="vertical" flexItem/>
            <CustomLink to="." sx={{color: '#fff'}} onClick={handleLogout}>Log out</CustomLink>
        </>
    )
}