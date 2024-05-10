import useSWRMutation, {SWRMutationResponse} from "swr/mutation";
import {addDeveloper} from "@/infrastructure/DeveloperClient";
import DeveloperDto from "@/domain/DeveloperDto";

export default function useAddDeveloper() {
    const {data, trigger, isMutating}: SWRMutationResponse<Response, any, "/api/v1/developers", DeveloperDto> =
        useSWRMutation("/api/v1/developers", addDeveloper);

    return {trigger}
}