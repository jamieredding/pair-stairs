import type CalculateInputDto from "../domain/CalculateInputDto";
import type {SaveCombinationEventDto} from "../domain/SaveCombinationEventDto.ts";
import type ScoredCombinationDto from "../domain/ScoredCombinationDto.ts";
import type CombinationEventDto from "../domain/CombinationEventDto.ts";

interface ErrorDto {
    errorCode: string;
}

export interface ApiError {
    errorCode: string
}

export async function calculateCombinations(url: string, {arg}: {
    arg: CalculateInputDto
}): Promise<ScoredCombinationDto[]> {
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    });

    if (res.status === 400) {
        let returnedErrorCode: string = "UNKNOWN";
        try {
            const {errorCode} = (await res.json()) as ErrorDto;
            returnedErrorCode = errorCode;
        } catch {
            /* ignored */
        }

        throw {errorCode: returnedErrorCode} as ApiError;
    }

    if (!res.ok) {
        throw {errorCode: "UNKNOWN"} as ApiError;
    }

    return res.json();
}

export function saveCombinationEvent(url: string, {arg}: { arg: SaveCombinationEventDto }) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    })
    // todo response handling
}

export async function getCombinationEvents(url: string): Promise<CombinationEventDto[]> {
    const res = await fetch(url);
    return await res.json();
}

export function deleteCombinationEvent(url: string, {arg: id}: { arg: number }) {
    return fetch(`${url}/${id}`, {
        method: "DELETE"
    })
    // todo response handling
}