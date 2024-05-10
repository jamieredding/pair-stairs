import useSWRMutation, {SWRMutationResponse} from "swr/mutation";
import {addStream} from "@/infrastructure/StreamClient";
import StreamDto from "@/domain/StreamDto";

export default function useAddStream() {
    const {data, trigger, isMutating}: SWRMutationResponse<Response, any, "/api/v1/streams", StreamDto> =
        useSWRMutation("/api/v1/streams", addStream);

    return {trigger}
}