import {useSWRConfig} from "swr";
import type DeveloperInfoDto from "../../domain/DeveloperInfoDto.ts";
import {DEVELOPER_INFO_PATH, getDeveloperInfos} from "../../infrastructure/DeveloperClient.ts";

export default function useRefreshDeveloperInfos() {
    const {mutate} = useSWRConfig()

    const configuredMutation = (potentialNewDeveloperInfo: DeveloperInfoDto) =>
        mutate(DEVELOPER_INFO_PATH, () => getDeveloperInfos(DEVELOPER_INFO_PATH), {
            optimisticData: (existingDeveloperInfos) => [...existingDeveloperInfos, potentialNewDeveloperInfo],
            rollbackOnError: true
        })

    return {refresh: configuredMutation};
}