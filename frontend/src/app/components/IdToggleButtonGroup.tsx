import {Button, Stack} from "@mui/material";

interface Displayable {
    id: number;
    displayName: string;
}

interface IdToggleButtonGroupProps {
    allItems: Displayable[];
    selectedIds: number[],
    setSelectedIds: (_: (prev: number[]) => number[]) => any
}

export default function IdToggleButtonGroup({allItems, selectedIds, setSelectedIds}: IdToggleButtonGroupProps) {
    function toggleItem(id: number) {
        if (selectedIds.includes(id)) {
            setSelectedIds(prev => prev.filter(other => other !== id))
        } else {
            setSelectedIds(prev => [...prev, id]);
        }
    }

    return (
        <Stack spacing={1} flexWrap="wrap" direction="row" useFlexGap={true}>
            {allItems.map(item =>
                <Button key={item.id} onClick={() => toggleItem(item.id)}
                        variant={selectedIds.includes(item.id) ? "contained" : "outlined"}
                >
                    {item.displayName}
                </Button>
            )}
        </Stack>
    )
}