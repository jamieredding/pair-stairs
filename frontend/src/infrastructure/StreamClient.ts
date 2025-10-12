import type StreamInfoDto from "../domain/StreamInfoDto.ts";
import type StreamDto from "../domain/StreamDto.ts";
import {handleErrors} from "./handleErrors.ts";
import type PatchStreamDto from "../domain/PatchStreamDto.ts";

export const STREAM_INFO_PATH = "/api/v1/streams/info"

export async function getStreamInfos(url: string): Promise<StreamInfoDto[]> {
    const response = await handleErrors(await fetch(url));

    return await response.json();
}

export async function addStream(url: string, {arg}: { arg: StreamDto }) {
    return await handleErrors(await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    }))
}

export async function patchStream(url: string, {arg}: { arg: { data: PatchStreamDto, id: number } }) {
    const response = await handleErrors(await fetch(`${url}/${arg.id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg.data)
    }))

    return response.json()
}
