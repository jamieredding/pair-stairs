import ScoredCombinationDto from "@/app/domain/ScoredCombinationDto";
import {Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {Fragment} from "react";

interface ScoredCombinationsProps {
    dtos: ScoredCombinationDto[];
}

export default function ScoredCombinations({dtos}: ScoredCombinationsProps) {
    const numberOfPairs = dtos[0].combination.length

    // todo highlight the selected column when selected
    return (
        <TableContainer>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell colSpan={1}/>
                        {dtos.map((_, index) =>
                            <TableCell key={index}  align="center" colSpan={2}>{index + 1}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.from({length: numberOfPairs}, (_, pairIndex) => (
                        <TableRow key={dtos[0].combination[pairIndex].stream.displayName}>
                            <TableCell colSpan={1}>{dtos[0].combination[pairIndex].stream.displayName}</TableCell>
                            {dtos.map((dto, index) =>
                                <Fragment key={index}>
                                    {dto.combination[pairIndex].developers.length === 2 &&
                                        <Fragment>
                                            <TableCell
                                                align="center">{dto.combination[pairIndex].developers[0].displayName}</TableCell>
                                            <TableCell
                                                align="center">{dto.combination[pairIndex].developers[1].displayName}</TableCell>
                                        </Fragment>
                                    }
                                    {dto.combination[pairIndex].developers.length === 1 &&
                                        <TableCell
                                            align="center"
                                            colSpan={2}>{dto.combination[pairIndex].developers[0].displayName}</TableCell>
                                    }
                                </Fragment>
                            )}
                        </TableRow>
                    ))}
                    <TableRow>
                        <TableCell>Score</TableCell>
                        {dtos.map((dto, index) =>
                            <TableCell key={index} align="center" colSpan={2}>{dto.score}</TableCell>
                        )}
                    </TableRow>
                    <TableRow>
                        <TableCell></TableCell>
                        {dtos.map((dto, index) =>
                            <TableCell key={index} align="center" colSpan={2}>
                                <Button variant="outlined">Accept</Button>
                            </TableCell>
                        )}
                    </TableRow>
                </TableBody>
            </Table>
        </TableContainer>
    )
}