import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";
import {deleteCombinationEvent} from "../../infrastructure/CombinationClient.ts";

export default function useDeleteCombinationEvent() {
    const {
        trigger,
        isMutating,
        error
    }: SWRMutationResponse<Response, never, "/api/v1/combinations/event", number> = useSWRMutation("/api/v1/combinations/event", deleteCombinationEvent);

    return {
        trigger,
        isError: error,
        isLoading: isMutating
    };
}