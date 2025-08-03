import type PairStreamDto from "./PairStreamDto";

export default interface CombinationEventDto {
    id: number;
    date: string;
    combination: PairStreamDto[];
}