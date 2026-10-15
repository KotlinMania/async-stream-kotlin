#if canImport(Testing)
import Testing
import AsyncStream

@Suite("AsyncStream Swift Export Tests")
struct AsyncStreamExportTests {
    @Test("AsyncStream swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "AsyncStream swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import AsyncStream

final class AsyncStreamExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "AsyncStream swift module imported cleanly")
    }
}
#endif
