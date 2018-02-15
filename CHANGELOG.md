# Changelog
All notable changes to this project will be documented in this file.

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


