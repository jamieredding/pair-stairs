import type DeveloperInfoDto from "../domain/DeveloperInfoDto.ts";
import type DeveloperDto from "../domain/DeveloperDto.ts";

export const DEVELOPER_INFO_PATH = "/api/v1/developers/info"

export async function getDeveloperInfos(url: string): Promise<DeveloperInfoDto[]> {
    const res = await fetch(url);
    return await res.json();
}

export function addDeveloper(url: string, {arg}: { arg: DeveloperDto }) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    })
    // todo response handling
}
