import HomePage from "../index/HomePage.tsx";
import useFeatureFlags from "../../hooks/featureFlags/useFeatureFlags.ts";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import {Typography} from "@mui/material";

export default function TeamHomePage() {
    const {featureFlags, isLoading, isError} = useFeatureFlags()
    return <>
        {isLoading && <Loading/>}
        {isError && <Error/>}
        {featureFlags && featureFlags.teamsEnabled
            ? <HomePage route="/team/$teamName"/>
            : <Typography variant="h4" color="error">Teams support is disabled.</Typography>
        }
    </>
}