import { createFileRoute } from '@tanstack/react-router'
import HomePage from "../components/index/HomePage.tsx";

export const Route = createFileRoute('/')({
    component: Index,
})

function Index() {
    return (
        <main>
            <HomePage/>
        </main>
    )
}