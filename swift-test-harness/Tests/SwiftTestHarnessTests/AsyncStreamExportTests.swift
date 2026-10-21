import Testing
import AsyncStream

@Suite("AsyncStream Swift Export Tests")
struct AsyncStreamExportTests {
    @Test("Swift module imports and basic types are reachable")
    func swiftModuleLoads() throws {
        #expect(Output.self == Void.self)
        #expect(String(describing: Receiver.self).contains("Receiver"))
        #expect(String(describing: Sender.self).contains("Sender"))
    }
}
