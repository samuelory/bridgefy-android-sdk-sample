# Changelog
All notable changes to this project will be documented in this file.

## [1.1.13] - 2018-10-12
### Changed
- Fixed a crash that involved resetting Bluetooth
- Fixed a bug with the connection decision algorithm
### Added
- A new callback on StateListener that warns when a device has been permanently blacklisted from connections


## [1.1.12] - 2018-09-17
### Changed
- Fixed a crash that involved stopping Bridgefy while connections were active


## [1.1.11] - 2018-07-09
### Changed
- Added a new profile BFConfigProfileShortReach
- onMessageSent(Message) has been deprecated in favor of onMessageSent(String)


## [1.1.10] - 2018-06-04
### Changed
- Fixed an connectivity issue involving Android Things and the Raspberry Pi


## [1.1.9] - 2018-04-22
### Changed
- Added hops field to Message.
- Fixed a threading issue.


## [1.1.8] - 2018-02-28
### Changed
- Added a new profile for messages.


## [1.1.7] - 2018-03-05
### Changed
- Solved a bug that would crash devices with Bluetooth off under certain circumstances


## [1.1.6] - 2018-02-28
### Changed
- Devices running Android Things are properly started now.


## [1.1.5] - 2018-01-22
### Changed
- Solved a bug that caused errors when receiving binary data from iOS devices

## [1.1.4] - 2018-01-22
### Changed
- Solved a bug that caused errors when sending binary data between Android and iOS

## [1.1.3] - 2018-01-16
### Added
- Added the new BFBleProfile class to handle Bluetooth 5 compatibility


## [1.1.2] - 2018-01-08
### Added
- Added the new engine profile BFConfigProfileNoFowarding

### Changed
- Methods for sending message in the Bridgefy class won't throw exceptions in case Bridgefy hasn't been initialized.
They will now return null.
- Fixed a bug that caused messages to be duplicated under certain circumstances.


## [1.1.1] - 2017-12-12
### Added
- Fixed a connectivity issue in Bluetooth LE mode


## [1.1.0] - 2017-12-06
### Added
- In addition to Bluetooth LE, there is now a Bluetooth Classic mode
- Deprecated Bridgefy.createMessage() methods. Use Message.Builder class to create Message Objects
- Deprecated ConnectionType enum. Use Config.Antenna instead


