import type DeveloperInfoDto from "../domain/DeveloperInfoDto.ts";
import type DeveloperDto from "../domain/DeveloperDto.ts";
import {handleErrors} from "./handleErrors.ts";
import type PatchDeveloperDto from "../domain/PatchDeveloperDto.ts";

export const DEVELOPER_INFO_PATH = "/api/v1/developers/info"

export async function getDeveloperInfos(url: string): Promise<DeveloperInfoDto[]> {
    const response = await handleErrors(await fetch(url));

    return await response.json();
}

export async function addDeveloper(url: string, {arg}: { arg: DeveloperDto }) {
    return await handleErrors(await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    }))
}

export async function patchDeveloper(url: string, {arg}: { arg: { data: PatchDeveloperDto, id: number } }) {
    const response = await handleErrors(await fetch(`${url}/${arg.id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg.data)
    }))

    return response.json()
}
