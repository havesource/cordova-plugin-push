// swift-tools-version:5.9

import PackageDescription

let firebaseMessagingSDKVersion: Version = "12.14.0"

let package = Package(
    name: "@havesource/cordova-plugin-push",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "@havesource/cordova-plugin-push",
            targets: ["@havesource/cordova-plugin-push"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/apache/cordova-ios.git", branch: "master"),
        .package(
            url: "https://github.com/firebase/firebase-ios-sdk.git",
            exact: firebaseMessagingSDKVersion
        )
    ],
    targets: [
        .target(
            name: "@havesource/cordova-plugin-push",
            dependencies: [
                .product(name: "Cordova", package: "cordova-ios"),
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk")
            ],
            path: "src/ios",
            resources: [],
            publicHeadersPath: "."
        )
    ]
)
