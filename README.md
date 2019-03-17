# Digital Ocean Spaces Android Example
This is an example project showing how to interact with [Digital Ocean Spaces](https://www.digitalocean.com/products/spaces/) from an Android app.

It relies on the official AWS S3 SDK to upload and download files from a Space, and is a Kotlin app.

🚨 **Note that this is purely sample code** - we don't claim to be showing best practices with this, nor should you ship this code as-is without first testing it yourself. It's meant to demonstrate _how_ you can connect to a Space using the S3 SDK, not how to build a Swift app.

This sample source code forms part of the Getting Started with Spaces course by The Cloud Hub.

## Getting started
To run this sample code:
1. Make sure you have Android Studio and the Android SDK installed
2. Open the project, and then open the `SpacesFileRepository.swift` file
3. Change the `accesskey`, `secretkey` to your Spaces API key (generated (here)[https://cloud.digitalocean.com/account/api/tokens]), `spacename` to the name of your Space on Digital Ocean and `spaceregion` to your Space' region

The project should run, and you'll be able to upload and download the sample image included in the app.
