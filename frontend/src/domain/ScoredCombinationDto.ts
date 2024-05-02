import PairStreamDto from "@/domain/PairStreamDto";

export default interface ScoredCombinationDto {
    score: number;
    combination: PairStreamDto[]
}