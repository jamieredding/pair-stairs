import useSWRMutation from "swr/mutation";
import {calculateCombinations} from "@/infrastructure/CombinationClient";

export default function usePostForCalculateCombinations() {
    const {data, trigger, isMutating, error} = useSWRMutation("/api/v1/combinations/calculate", calculateCombinations)

    return {
        combinations: data,
        trigger,
        isError: error,
        isLoading: isMutating
    }
}