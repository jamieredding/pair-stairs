import {Button, Stack, Table, TableBody, TableCell, TableContainer, TableRow} from "@mui/material";
import {Close} from "@mui/icons-material";
import PairStreamDto from "@/domain/PairStreamDto";
import {Fragment} from "react";

interface ManualSelectionTableProps {
    combination: PairStreamDto[]
    removeFromCombination: (pair: PairStreamDto) => void;
}

export default function ManualSelectionTable({combination, removeFromCombination}: ManualSelectionTableProps) {
    const numberOfPairs = combination.length;
    return (
        <Stack gap={1}>
            {combination.length === 0 &&
                <p>Make some pairs above</p>
            }
            {combination.length > 0 &&

                <TableContainer>
                    <Table>
                        <TableBody>
                            {Array.from({length: numberOfPairs}, (_, pairIndex) => (
                                <TableRow key={combination[pairIndex].stream.displayName}>
                                    <TableCell colSpan={1}>
                                        {combination[pairIndex].stream.displayName}
                                    </TableCell>
                                    {combination[pairIndex].developers.length === 2 &&
                                        <Fragment>
                                            <TableCell align="center">
                                                {combination[pairIndex].developers[0].displayName}
                                            </TableCell>
                                            <TableCell align="center">
                                                {combination[pairIndex].developers[1].displayName}
                                            </TableCell>
                                        </Fragment>
                                    }
                                    {combination[pairIndex].developers.length === 1 &&
                                        <TableCell align="center" colSpan={2}>
                                            {combination[pairIndex].developers[0].displayName}
                                        </TableCell>
                                    }
                                    <TableCell align="right">
                                        <Button
                                            onClick={() => removeFromCombination(combination[pairIndex])}><Close/></Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            }

        </Stack>
    )
}