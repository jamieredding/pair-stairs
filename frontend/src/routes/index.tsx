import {createFileRoute} from '@tanstack/react-router'
import HomePage from "../components/index/HomePage.tsx";
import {z} from "zod";

export const Route = createFileRoute('/')({
    component: Index,
    validateSearch: z.object({
        newCombinationTab: z.enum(['calculate', 'manual']).catch('calculate'),
    }),
})

function Index() {
    return (
        <main>
            <HomePage/>
        </main>
    )
}