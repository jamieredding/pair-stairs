import HomePage from "../index/HomePage.tsx";
import useFeatureFlags from "../../hooks/featureFlags/useFeatureFlags.ts";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import {AlertTitle, Typography} from "@mui/material";
import {getRouteApi} from "@tanstack/react-router";
import useGetTeamBySlug from "../../hooks/teams/useGetTeamBySlug.ts";
import ErrorSnackbar from "../ErrorSnackbar.tsx";
import type {ReactElement} from "react";

export default function TeamHomePage() {
    const {featureFlags, isLoading, isError} = useFeatureFlags()
    return <>
        {isLoading && <Loading/>}
        {isError && <Error/>}
        {featureFlags && featureFlags.teamsEnabled
            ? <EnabledTeamHomePage/>
            : <Typography variant="h4" color="error">Teams support is disabled.</Typography>
        }
    </>
}

function EnabledTeamHomePage() {
    const {teamName} = getRouteApi("/team/$teamName").useParams();
    const {teamDto, isLoading, isError} = useGetTeamBySlug(teamName);

    return (<>
            {isLoading && <Loading/>}
            <ErrorSnackbar error={isError} alertContent={alertContent}/>
            {teamDto === undefined
                ? <Typography variant="h2">Team [{teamName}] does not exist</Typography>
                : <>
                    <Typography variant="h2">{teamDto.name}</Typography>
                    <HomePage route="/team/$teamName"/>
                </>
            }
        </>
    )
}

function alertContent(errorCode: string): ReactElement {
    return <>
        <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
        Unable to load team
    </>
}
