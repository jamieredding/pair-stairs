import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";
import type StreamDto from "../../domain/StreamDto";
import { addStream } from "../../infrastructure/StreamClient";

export default function useAddStream() {
    const {trigger}: SWRMutationResponse<Response, never, "/api/v1/streams", StreamDto> =
        useSWRMutation("/api/v1/streams", addStream);

    return {trigger}
}