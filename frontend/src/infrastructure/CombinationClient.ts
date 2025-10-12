import type CalculateInputDto from "../domain/CalculateInputDto";
import type {SaveCombinationEventDto} from "../domain/SaveCombinationEventDto.ts";
import type ScoredCombinationDto from "../domain/ScoredCombinationDto.ts";
import type CombinationEventDto from "../domain/CombinationEventDto.ts";
import {handleErrors} from "./handleErrors.ts";

export async function calculateCombinations(url: string, {arg}: {
    arg: CalculateInputDto
}): Promise<ScoredCombinationDto[]> {
    const res = await handleErrors(await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    }));

    return res.json();
}

export async function saveCombinationEvent(url: string, {arg}: { arg: SaveCombinationEventDto }) {
    return await handleErrors(await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(arg)
        })
    )
}

export async function getCombinationEvents(url: string): Promise<CombinationEventDto[]> {
    const res = await handleErrors(await fetch(url));
    return await res.json();
}

export async function deleteCombinationEvent(url: string, {arg: id}: { arg: number }) {
    return await handleErrors(await fetch(`${url}/${id}`, {
        method: "DELETE"
    }))
}