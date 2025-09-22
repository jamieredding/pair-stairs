import type TeamDto from "../domain/TeamDto.ts";
import {handleErrors} from "./handleErrors.ts";

export async function addTeam(url: string, {arg}: { arg: TeamDto }): Promise<TeamDto> {
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    });

    await handleErrors(response)

    return response.json();
}

export async function getTeamBySlug(slug: string): Promise<TeamDto | undefined> {
    const response = await fetch(`/api/v1/teams/${slug}`, {headers: {"Accept": "application/json"}})

    switch (response.status) {
        case 200:
            return response.json()
        case 404:
            return undefined
        // default: TODO how to handle errors? should the return type encompass them?
        //     await handleErrors(response)
    }
}
