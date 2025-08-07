import * as React from "react";
import {Link, type LinkProps} from "@mui/material";
import {createLink, type LinkComponent} from "@tanstack/react-router";

// following instructions from here https://tanstack.com/router/latest/docs/framework/react/guide/custom-link#link
// also example here https://github.com/TanStack/router/blob/8efac91d6d7d3e12ed5ef1964392241142492f6a/examples/react/start-material-ui/src/components/CustomLink.tsx

const MUILinkComponent = React.forwardRef<HTMLAnchorElement, LinkProps>(
    (props, ref) => <Link ref={ref} {...props} />
)
const CreatedLinkComponent = createLink(MUILinkComponent)

export const CustomLink: LinkComponent<typeof MUILinkComponent> = (props) => {
    return <CreatedLinkComponent preload={"intent"} {...props}/>
}