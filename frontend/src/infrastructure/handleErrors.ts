import type {ErrorDto} from "../domain/ErrorDto.ts";
import type {ApiError} from "../domain/ApiError.ts";

export async function handleErrors(response: Response) {
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

    return response
}