export interface SaveCombinationEventDto {
    date: string;
    combination: PairStreamByIds[];
}

export interface PairStreamByIds {
    developerIds: number[];
    streamId: number
}