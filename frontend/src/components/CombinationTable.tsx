import {Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography} from "@mui/material";
import PairStreamDto from "@/domain/PairStreamDto";
import {Fragment} from "react";
import CloseButton from "@/components/CloseButton";

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

    const h6 = (text: string) => <Typography variant="h6">{text}</Typography>;

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
                                {combination[pairIndex].developers.length === 2 &&
                                    <Fragment>
                                        <TableCell align="center">
                                            {h6(combination[pairIndex].developers[0].displayName)}
                                        </TableCell>
                                        <TableCell align="center">
                                            {h6(combination[pairIndex].developers[1].displayName)}
                                        </TableCell>
                                    </Fragment>
                                }
                                {combination[pairIndex].developers.length === 1 &&
                                    <TableCell align="center" colSpan={2}>
                                        {h6(combination[pairIndex].developers[0].displayName)}
                                    </TableCell>
                                }
                                {removeFromCombination &&
                                    <TableCell align="right">
                                        <CloseButton onClick={() => handleRemoveFromCombination(combination[pairIndex])}/>
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