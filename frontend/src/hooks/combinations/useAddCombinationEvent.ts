import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";
import type {SaveCombinationEventDto} from "../../domain/SaveCombinationEventDto.ts";
import {saveCombinationEvent} from "../../infrastructure/CombinationClient.ts";

export default function useAddCombinationEvent() {
    const {
        trigger,
        isMutating,
        error
    }: SWRMutationResponse<Response, never, "/api/v1/combinations/event", SaveCombinationEventDto> =
        useSWRMutation("/api/v1/combinations/event", saveCombinationEvent)

    return {
        trigger,
        isError: error,
        isLoading: isMutating
    }
}