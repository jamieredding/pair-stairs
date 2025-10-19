import useSWRInfinite, {type SWRInfiniteResponse} from "swr/infinite";
import type CalculateInputDto from "../../domain/CalculateInputDto.ts";
import type ScoredCombinationDto from "../../domain/ScoredCombinationDto.ts";
import {calculateCombinations} from "../../infrastructure/CombinationClient.ts";
import type {ApiError} from "../../domain/ApiError.ts";
import type PageDto from "../../domain/PageDto.ts";

export default function useCalculateCombinations(requestBody: CalculateInputDto) {
    const {
        data,
        error,
        isLoading,
        setSize
    }: SWRInfiniteResponse<PageDto<ScoredCombinationDto>, ApiError> =
        useSWRInfinite(
            (pageIndex) => [
                `/api/v1/combinations/calculate?page=${pageIndex}&projection=page`,
                requestBody
            ],
            ([url]) => calculateCombinations(url, {arg: requestBody})
        );

    return {
        combinationsPages: data,
        isError: error,
        isLoading,
        setSize
    }
}