import {createFileRoute} from '@tanstack/react-router'
import TeamHomePage from "../components/teams/TeamHomePage.tsx";
import {z} from "zod";

export const Route = createFileRoute('/team/$teamName')({
    component: TeamHomePage,
    validateSearch: z.object({
        newCombinationTab: z.enum(['calculate', 'manual']).catch('calculate'),
    })
})
