import {Button, Stack} from "@mui/material";
import {Close} from "@mui/icons-material";
import PairStreamDto from "@/app/domain/PairStreamDto";

interface ManualSelectionTableProps {
    combination: PairStreamDto[]
    removeFromCombination: (pair: PairStreamDto) => void;
}

export default function ManualSelectionTable({combination, removeFromCombination}: ManualSelectionTableProps) {
    return (
        <Stack gap={1}>
            <div>Selected</div>
            {combination.length === 0 &&
                <p>Make some pairs above</p>
            }
            {combination.map(pair =>
                <div>
                    {pair.stream.displayName}, {pair.developers.map(dev => dev.displayName)}
                    <Button onClick={() => removeFromCombination(pair)}><Close/></Button>
                </div>
            )}
        </Stack>
    )
}