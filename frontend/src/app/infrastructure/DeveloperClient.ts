import useSWR, {useSWRConfig} from "swr";
import DeveloperInfoDto from "@/app/domain/DeveloperInfoDto";
import useSWRMutation from "swr/mutation";
import DeveloperDto from "@/app/domain/DeveloperDto";

const DEVELOPER_INFO_PATH = "/api/v1/developers/info"

export function useGetDeveloperInfos() {
    const {data, error, isLoading} = useSWR(DEVELOPER_INFO_PATH, getDeveloperInfos)

    return {
        allDevelopers: data,
        isLoading,
        isError: error
    }
}

export function useRefreshGetDeveloperInfo() {
    const {mutate} = useSWRConfig()

    const configuredMutation = (potentialNewDeveloperInfo: DeveloperInfoDto) =>
        mutate(DEVELOPER_INFO_PATH, () => getDeveloperInfos(DEVELOPER_INFO_PATH), {
            optimisticData: (existingDeveloperInfos) => [...existingDeveloperInfos, potentialNewDeveloperInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}

export function useAddDeveloper() {
    const {data, trigger, isMutating} = useSWRMutation("/api/v1/developers", addDeveloper);

    return {trigger}
}

async function getDeveloperInfos(url: string): Promise<DeveloperInfoDto[]> {
    const res = await fetch(url);
    return await res.json();
}

function addDeveloper(url: string, {arg}: { arg: DeveloperDto }) {
    return fetch(url, {method: "POST", body: JSON.stringify(arg)})
    // todo response handling
}
