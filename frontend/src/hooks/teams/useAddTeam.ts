import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";
import type TeamDto from "../../domain/TeamDto.ts";
import {addTeam} from "../../infrastructure/TeamClient.ts";

export default function useAddTeam() {
    const {trigger}: SWRMutationResponse<Response, never, "/api/v1/teams", TeamDto> =
        useSWRMutation("/api/v1/teams", addTeam);

    return {trigger}
}