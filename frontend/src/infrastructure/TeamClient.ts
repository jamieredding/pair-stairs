import type TeamDto from "../domain/TeamDto.ts";
import type {ErrorDto} from "../domain/ErrorDto.ts";
import type {ApiError} from "../domain/ApiError.ts";

export async function addTeam(url: string, {arg}: { arg: TeamDto }): Promise<TeamDto> {
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(arg)
    });

    if (response.status === 400) {
        let returnedErrorCode: string = "UNKNOWN";
        try {
            const {errorCode} = (await response.json()) as ErrorDto;
            returnedErrorCode = errorCode;
        } catch {
            /* ignored */
        }

        throw {errorCode: returnedErrorCode} as ApiError;
    }

    if (!response.ok) {
        throw {errorCode: "UNKNOWN"} as ApiError;
    }

    return response.json();
}
