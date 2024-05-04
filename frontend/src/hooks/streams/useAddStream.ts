import useSWRMutation from "swr/mutation";
import {addStream} from "@/infrastructure/StreamClient";

export default function useAddStream() {
    const {data, trigger, isMutating} = useSWRMutation("/api/v1/streams", addStream);

    return {trigger}
}