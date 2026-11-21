import { TestBed } from "@angular/core/testing";
import { Terminosservice } from "./terminosservice";

describe("Terminosservice", () => {
  let service: Terminosservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Terminosservice);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
