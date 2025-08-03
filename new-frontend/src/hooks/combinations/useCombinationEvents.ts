import useSWRInfinite, {type SWRInfiniteKeyLoader, type SWRInfiniteResponse} from "swr/infinite";
import type CombinationEventDto from "../../domain/CombinationEventDto.ts";
import {getCombinationEvents} from "../../infrastructure/CombinationClient.ts";

export const getKey: SWRInfiniteKeyLoader = (pageIndex, previousPageData) => {
    if (previousPageData && !previousPageData.length) {
        return null
    }
    return `/api/v1/combinations/event?page=${pageIndex}`

}

export default function useCombinationEvents() {
    const {data, error, isLoading, setSize}: SWRInfiniteResponse<CombinationEventDto[]> =
        useSWRInfinite(getKey, (url: string) => getCombinationEvents(url));

    return {
        combinationEvents: data,
        isLoading,
        isError: error,
        setSize
    }
}