import {describe, it} from "vitest";
import HomePage from "./HomePage";
import {render} from "@testing-library/react";

describe("HomePage", () => {
    it("renders without crashing", () => {
        render(<HomePage/>)
    });
});