import {createFileRoute} from '@tanstack/react-router'
import {Typography} from "@mui/material";
import TeamHomePage from "../components/teams/TeamHomePage.tsx";
import {z} from "zod";

export const Route = createFileRoute('/team/$teamName')({
    loader: async ({params}) => params.teamName,
    component: RouteComponent,
    validateSearch: z.object({
        newCombinationTab: z.enum(['calculate', 'manual']).catch('calculate'),
    })
})

function RouteComponent() {
    const {teamName} = Route.useParams()
    return <>
        <Typography variant="h2">{teamName}</Typography>
        <TeamHomePage />
    </>
}
