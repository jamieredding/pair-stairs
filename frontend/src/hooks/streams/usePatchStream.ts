import useSWRMutation from "swr/mutation";
import {patchStream} from "../../infrastructure/StreamClient.ts";

export default function usePatchStream() {
    const {trigger, error, isMutating} = useSWRMutation(`/api/v1/streams`, patchStream, );

    return {trigger, isError: error, isLoading: isMutating}
}