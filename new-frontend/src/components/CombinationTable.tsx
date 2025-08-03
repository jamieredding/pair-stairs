import {Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography} from "@mui/material";
import type PairStreamDto from "../domain/PairStreamDto.ts";
import CloseButton from "./CloseButton.tsx";

interface CombinationTableProps {
    combination: PairStreamDto[]
    removeFromCombination?: (pair: PairStreamDto) => void;
}

export default function CombinationTable({combination, removeFromCombination}: CombinationTableProps) {
    const numberOfPairs = combination.length;

    function handleRemoveFromCombination(pair: PairStreamDto) {
        if (removeFromCombination) {
            removeFromCombination(pair);
        }
    }

    const h6 = (text: string) => <Typography variant="h6" component="p">{text}</Typography>;

    return (
        <Stack gap={1}>
            <TableContainer>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell colSpan={1}>{h6("Stream")}</TableCell>
                            <TableCell colSpan={2} align="center">{h6("Developers")}</TableCell>
                            {removeFromCombination &&
                                <TableCell colSpan={1} align="right">{h6("Remove")}</TableCell>
                            }
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {Array.from({length: numberOfPairs}, (_, pairIndex) => (
                            <TableRow key={combination[pairIndex].stream.displayName}>
                                <TableCell colSpan={1}>
                                    {h6(combination[pairIndex].stream.displayName)}
                                </TableCell>
                                {combination[pairIndex].developers.map(developer =>
                                    <TableCell key={developer.id} align="center"
                                               colSpan={2 / combination[pairIndex].developers.length}>
                                        {h6(developer.displayName)}
                                    </TableCell>
                                )}
                                {removeFromCombination &&
                                    <TableCell align="right">
                                        <CloseButton
                                            onClick={() => handleRemoveFromCombination(combination[pairIndex])}/>
                                    </TableCell>
                                }
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Stack>
    )
}