#pragma once
// ============================================
// WiFi Configuration
// Never commit this file to version control!
// ============================================

// Network Selection: Change this to switch networks
// 0 = Home, 1 = School
#define ACTIVE_NETWORK 0

// Home Network
#define HOME_SSID       "YOUR_HOME_SSID"
#define HOME_PASSWORD   "YOUR_HOME_PASSWORD"

// School Network
#define SCHOOL_SSID     "YOUR_SCHOOL_SSID"
#define SCHOOL_PASSWORD "YOUR_SCHOOL_PASSWORD"

// Active credentials (automatically set based on ACTIVE_NETWORK)
#if ACTIVE_NETWORK == 0
  #define WIFI_SSID     HOME_SSID
  #define WIFI_PASSWORD HOME_PASSWORD
#else
  #define WIFI_SSID     SCHOOL_SSID
  #define WIFI_PASSWORD SCHOOL_PASSWORD
#endif
// Add other secrets as needed
