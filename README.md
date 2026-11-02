# EBS260 Sender

**Proof of Concept (POC) – Android Application**

EBS260 Sender is an experimental Android application designed to demonstrate communication with the EBS260 industrial inkjet printer over a Wi-Fi network.

The project is intended for personal use, experimentation, and further development.

## Project Status

**Proof of Concept (POC)**

The application is under development and should not be considered production-ready.

Features, implementation details, and user interfaces may change.

## Technology Stack

- Kotlin
- Android SDK
- Jetpack Compose
- Gradle (Kotlin DSL)

## Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/WoelkeK/EBS260_Sender.git
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronize the project.
4. Connect an Android device or start an emulator.
5. Build and run the application.

## Printer Configuration

To establish communication between the Android application and the EBS260 printer, ensure the following requirements are met:

1. **Network connection:** The Android device and the EBS260 printer and the Android mobile device must be connected to the same Wi-Fi network.
2. **Print layout:** The print layout configured on the printer must include a communication field (COMM).
3. **Socket configuration:** Configure the COMM field to use `Socket 1` with port `5001`.
4. **Printer IP address:** Obtain the printer's IP address from its Wi-Fi manager or the router's administration interface.
5. **Connectivity:** Ensure that TCP communication on port `5001` is permitted by the network configuration.
6. 
## Disclaimer

This project is provided for experimental and educational purposes.

The software is supplied without any warranty and is not guaranteed to be suitable for production environments.

## License

This project is licensed under the MIT License.

See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 WoelkeK
