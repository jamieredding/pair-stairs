import CombinationEventDto from "@/domain/CombinationEventDto";
import {getCombinationEvents} from "@/infrastructure/CombinationClient";
import useSWRInfinite, {SWRInfiniteKeyLoader, SWRInfiniteResponse} from "swr/infinite";

const getKey: SWRInfiniteKeyLoader = (pageIndex, previousPageData) => {
    if (previousPageData && !previousPageData.length) {
        return null
    }
    return `/api/v1/combinations/event?page=${pageIndex}`

}

export default function useCombinationEvents() {
    const {data, error, isLoading, setSize}: SWRInfiniteResponse<CombinationEventDto[], any> =
        useSWRInfinite(getKey, (url: string) => getCombinationEvents(url));

    return {
        combinationEvents: data,
        isLoading,
        isError: error,
        setSize
    }
}