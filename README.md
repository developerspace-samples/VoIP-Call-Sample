# VoIP-Call-Sample
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](#)
[![Platform](https://img.shields.io/badge/Language-Kotlin-yellowgreen.svg)](#)
![GitHub stars](https://img.shields.io/github/stars/developerspace-samples/VoIP-Call-Sample?style=social)

An android application which includes ConnectionService binding to handle incoming & outgoing calls. It uses [CometChat UI Kit Library](https://github.com/cometchat-pro/android-java-chat-app) for Voice & Video Calling Services and Firebase Cloud Messaging for Push Notifications. Whenever the app is in background or killed state. Firebase Push Notification helps to handle incoming calls.

[![VoIP Sample](https://img.youtube.com/vi/tINaAzPGRIU/0.jpg)](https://www.youtube.com/watch?v=tINaAzPGRIU)

---

## Pre-requisites
 :heavy_check_mark: Android studio installed in your system.<br/>
 :heavy_check_mark: Android Device or Emulator to run your app.<br/>
 :heavy_check_mark: Setup Account on Firebase and integrate app with your Firebase Project.<br/>
 :heavy_check_mark: Setup Account on CometChat and create a App.

--- 

## Setup :hammer:

- You can clone the project from the Sample VoIP Calling repository.

```// Clone this repository
  git clone https://github.com/developerspace-samples/VoIP-Call-Sample.git
```

- It's necessary to have CometChat Account to run the app. 
You can create your account in CometChat and replace the credentials with yours.<br/>
<img src="https://cdn-images-1.medium.com/max/720/1*9uQ_TqkxBtY_j7ZbD4j9Yw.png"/>

- Also make sure to create a Firebase Project and set-up with this app. You need to add `google-service.json` file of yout Firebase project in your `app` folder.
For more details please check the below link.
https://firebase.google.com/docs/android/setup

- Once the firebase setup is done you need to add and enable Push Notification Extension from CometChat Dashboard.
Please check below link to know more about enhanced Push Notification in CometChat
https://prodocs.cometchat.com/docs/android-extensions-enhanced-push-notification

- Once the above configurations are done you can open and run the project in Android Studio.

---

<div style="width:100%">
	<div style="width:50%; display:inline-block">
		<h2 align="center">
      :handshake: Open for Contribution
		</h2>	
	</div>	
</div>
