import Foundation

@objc public class tools: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
