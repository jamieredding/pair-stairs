import {createFileRoute} from '@tanstack/react-router'
import {Typography} from "@mui/material";
import TeamHomePage from "../components/teams/TeamHomePage.tsx";
import {z} from "zod";
import {getTeamBySlug} from "../infrastructure/TeamClient.ts";
import type TeamDto from "../domain/TeamDto.ts";

export const Route = createFileRoute('/team/$teamName')({
    loader: async ({params}) => getTeamBySlug(params.teamName),
    component: RouteComponent,
    validateSearch: z.object({
        newCombinationTab: z.enum(['calculate', 'manual']).catch('calculate'),
    })
})

function RouteComponent() {
    const team: TeamDto | undefined = Route.useLoaderData();
    const {teamName} = Route.useParams()

    if (!team) {
        return <Typography variant="h2">Team [{teamName}] does not exist</Typography>
    }

    return <>
        <Typography variant="h2">{team.name}</Typography>
        <TeamHomePage />
    </>
}
