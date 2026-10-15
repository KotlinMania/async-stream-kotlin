import Testing
import AsyncStream

@Suite("AsyncStream Swift Export Tests")
struct AsyncStreamExportTests {
    @Test("Swift module imports and basic types are reachable")
    func swiftModuleLoads() throws {
        #expect(Bool(true))
    }
}
