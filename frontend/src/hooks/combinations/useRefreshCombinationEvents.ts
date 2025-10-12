import {useSWRConfig} from "swr";
import {unstable_serialize} from "swr/infinite";
import {getKey} from "./useCombinationEvents.ts";

export default function useRefreshCombinationEvents() {
    const {mutate} = useSWRConfig()

    // todo error handling?
    const configuredMutation: () => Promise<unknown> = () => mutate(unstable_serialize(getKey))

    return {refresh: configuredMutation};
}