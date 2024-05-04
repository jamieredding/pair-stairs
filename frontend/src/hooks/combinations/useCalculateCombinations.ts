import {calculateCombinations} from "@/infrastructure/CombinationClient";
import useSWRInfinite, {SWRInfiniteKeyLoader} from "swr/infinite";
import CalculateInputDto from "@/domain/CalculateInputDto";

const getKey :SWRInfiniteKeyLoader = (pageIndex, previousPageData) => {
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
    } = useSWRInfinite(getKey, (url:string ) => calculateCombinations(url, {arg: requestBody}));

    return {
        combinationsPages: data,
        isError: error,
        isLoading,
        setSize
    }
}