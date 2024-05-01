import useSWRMutation from "swr/mutation";
import CalculateInputDto from "@/app/domain/CalculateInputDto";
import ScoredCombinationDto from "@/app/domain/ScoredCombinationDto";

export function usePostForCalculateCombinations() {
    const {data, trigger, isMutating, error} = useSWRMutation("/api/v1/combinations/calculate", calculateCombinations)

    return {
        combinations: data,
        trigger,
        isError: error,
        isLoading: isMutating
    }
}

function calculateCombinations(url: string, {arg}: { arg: CalculateInputDto }): Promise<ScoredCombinationDto[]> {
    return fetch(url, {method: "POST", body: JSON.stringify(arg)})
        .then(res => res.json())
}