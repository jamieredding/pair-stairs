import type DeveloperDto from "../../domain/DeveloperDto.ts";
import {addDeveloper} from "../../infrastructure/DeveloperClient.ts";
import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";

export default function useAddDeveloper() {
    const {trigger, isMutating, error}: SWRMutationResponse<Response, never, "/api/v1/developers", DeveloperDto> =
        useSWRMutation("/api/v1/developers", addDeveloper);

    return {trigger, isError: error, isLoading: isMutating}
}