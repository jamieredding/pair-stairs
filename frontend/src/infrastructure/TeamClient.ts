import type TeamDto from "../domain/TeamDto.ts";

export function addTeam(url: string, {arg}: { arg: TeamDto }) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    })
    // todo response handling
}
