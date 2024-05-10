import useSWRMutation, {SWRMutationResponse} from "swr/mutation";
import {saveCombinationEvent} from "@/infrastructure/CombinationClient";
import SaveCombinationEventDto from "@/domain/SaveCombinationEventDto";

export default function useAddCombinationEvent() {
    const {
        data,
        trigger,
        isMutating,
        error
    }: SWRMutationResponse<Response, any, "/api/v1/combinations/event", SaveCombinationEventDto> =
        useSWRMutation("/api/v1/combinations/event", saveCombinationEvent)

    return {
        trigger,
        isError: error,
        isLoading: isMutating
    }
}