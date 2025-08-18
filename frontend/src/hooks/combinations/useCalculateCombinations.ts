import useSWRInfinite, {type SWRInfiniteKeyLoader, type SWRInfiniteResponse} from "swr/infinite";
import type CalculateInputDto from "../../domain/CalculateInputDto.ts";
import type ScoredCombinationDto from "../../domain/ScoredCombinationDto.ts";
import {type ApiError, calculateCombinations} from "../../infrastructure/CombinationClient.ts";

const getKey: SWRInfiniteKeyLoader = (pageIndex, previousPageData) => {
    if (previousPageData && !previousPageData.length) {
        return null
    }
    return `/api/v1/combinations/calculate?page=${pageIndex}`
}

export default function useCalculateCombinations(requestBody: CalculateInputDto) {
    const {
        data,
        error,
        isLoading,
        setSize
    }: SWRInfiniteResponse<ScoredCombinationDto[], ApiError> =
        useSWRInfinite(getKey, (url: string) => calculateCombinations(url, {arg: requestBody}));

    return {
        combinationsPages: data,
        isError: error,
        isLoading,
        setSize
    }
}