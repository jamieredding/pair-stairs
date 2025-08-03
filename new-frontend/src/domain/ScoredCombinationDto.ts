import type PairStreamDto from "./PairStreamDto";

export default interface ScoredCombinationDto {
    score: number;
    combination: PairStreamDto[]
}