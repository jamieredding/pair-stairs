import useSWRMutation from "swr/mutation";
import {saveCombinationEvent} from "@/infrastructure/CombinationClient";

export default function useAddCombinationEvent() {
    const {data, trigger, isMutating, error} = useSWRMutation("/api/v1/combinations/event", saveCombinationEvent)

    return {
        trigger,
        isError: error,
        isLoading: isMutating
    }
}