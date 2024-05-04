import useSWRMutation from "swr/mutation";
import {addDeveloper} from "@/infrastructure/DeveloperClient";

export default function useAddDeveloper() {
    const {data, trigger, isMutating} = useSWRMutation("/api/v1/developers", addDeveloper);

    return {trigger}
}