import HomePage from "../index/HomePage.tsx";
import Loading from "../Loading.tsx";
import {AlertTitle, Typography} from "@mui/material";
import {getRouteApi} from "@tanstack/react-router";
import useGetTeamBySlug from "../../hooks/teams/useGetTeamBySlug.ts";
import ErrorSnackbar from "../ErrorSnackbar.tsx";
import type {ReactElement} from "react";
import FeatureFlag from "../FeatureFlag.tsx";
import {teamsEnabled} from "../../utils/featureFlags.ts";

export default function TeamHomePage() {
    return (
        <FeatureFlag on={teamsEnabled}
                     showFeatureFlagFetching={true}
                     textWhenDisabled="Teams support is disabled."
        >
            <EnabledTeamHomePage/>
        </FeatureFlag>
    )
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
