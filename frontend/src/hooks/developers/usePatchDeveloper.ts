import {patchDeveloper} from "../../infrastructure/DeveloperClient.ts";
import useSWRMutation from "swr/mutation";

export default function usePatchDeveloper() {
    const {trigger, error, isMutating} = useSWRMutation(`/api/v1/developers`, patchDeveloper, );

    return {trigger, isError: error, isLoading: isMutating}
}