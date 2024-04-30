import {Button, Stack} from "@mui/material";

interface Displayable {
    id: number;
    displayName: string;
}

interface IdToggleButtonGroupProps {
    allItems: Displayable[];
    selectedIds: number[];
    setSelectedIds: (_: (prev: number[]) => number[]) => any;
    maxSelectable?: number;
}

export default function IdToggleButtonGroup({
                                                allItems,
                                                selectedIds,
                                                setSelectedIds,
                                                maxSelectable
                                            }: IdToggleButtonGroupProps) {
    function toggleItem(id: number) {
        if (selectedIds.includes(id)) {
            setSelectedIds(prev => prev.filter(other => other !== id))
        } else {
            if (maxSelectable) {
                if (selectedIds.length < maxSelectable) {
                    setSelectedIds(prev => [...prev, id]);
                } else if (maxSelectable === 1) { // usability to allow toggling between single options
                    setSelectedIds(_ => [id])
                }
            } else {
                setSelectedIds(prev => [...prev, id]);
            }
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