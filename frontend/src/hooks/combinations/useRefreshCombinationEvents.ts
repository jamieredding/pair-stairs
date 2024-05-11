import {useSWRConfig} from "swr";
import {unstable_serialize} from "swr/infinite";
import {getKey} from "@/hooks/combinations/useCombinationEvents";

export default function useRefreshCombinationEvents() {
    const {mutate} = useSWRConfig()

    const configuredMutation: () => Promise<any> = () => mutate(unstable_serialize(getKey))

    return {refresh: configuredMutation};
}