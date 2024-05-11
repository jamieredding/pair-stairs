import useSWRMutation, {SWRMutationResponse} from "swr/mutation";
import {deleteCombinationEvent} from "@/infrastructure/CombinationClient";

export default function useDeleteCombinationEvent() {
    const {
        trigger,
        isMutating,
        error
    }: SWRMutationResponse<Response, any, "/api/v1/combinations/event", number> = useSWRMutation("/api/v1/combinations/event", deleteCombinationEvent);

    return {
        trigger,
        isError: error,
        isLoading: isMutating
    };
}