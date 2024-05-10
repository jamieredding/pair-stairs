import {describe, it} from "vitest";
import {render} from "@testing-library/react";
import HomePage from "@/app/{home}/components/HomePage";

describe("HomePage", () => {
    it("renders without crashing", () => {
        render(<HomePage/>)
    });
});