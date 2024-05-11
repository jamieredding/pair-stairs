import CalculateInputDto from "@/domain/CalculateInputDto";
import ScoredCombinationDto from "@/domain/ScoredCombinationDto";
import SaveCombinationEventDto from "@/domain/SaveCombinationEventDto";
import CombinationEventDto from "@/domain/CombinationEventDto";

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
    return await res.json();
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

export function deleteCombinationEvent(url: string, {arg: id} : {arg: number}) {
    return fetch(`${url}/${id}`, {
        method: "DELETE"
    })
    // todo response handling
}