const jsonResponse = {
  "policyConfigurations": {
    "androidPolicy": {
      "policy": [
        {
          "name": "Passcode Policy",
          "panels": [
            {
              "panel":{
                "panelId": "PASSCODE_POLICY",
                "panelItem": [
                  {
                    "label": "Allow Simple Value",
                    "tooltip": "Permits repeating, ascending and descending character sequences",
                    "optional": {
                      "checked": true
                    },
                    "type": "checkbox",
                    "id": "allowSimple"
                  },
                  {
                    "label": "Require alphanumeric value",
                    "tooltip": "Mandates to contain both letters and numbers",
                    "optional": {
                      "checked": true
                    },
                    "type": "checkbox",
                    "id": "requireAlphanumeric"
                  },
                  {
                    "label": "Minimum passcode length",
                    "tooltip": "Minimum number of characters allowed in a passcode",
                    "optional": {
                      "option": [
                        {
                          "name" : "None",
                          "value": "",
                        },
                        {
                          "name" : "04",
                          "value": "4",
                        },
                        {
                          "name" : "05",
                          "value": "5",
                        },
                        {
                          "name" : "06",
                          "value": "6",
                        },
                        {
                          "name" : "07",
                          "value": "7",
                        },
                        {
                          "name" : "08",
                          "value": "8",
                        },
                        {
                          "name" : "09",
                          "value": "9",
                        },
                        {
                          "name" : "10",
                          "value": "10",
                        },
                        {
                          "name" : "11",
                          "value": "11",
                        },
                        {
                          "name" : "12",
                          "value": "12",
                        },
                        {
                          "name" : "13",
                          "value": "13",
                        },
                        {
                          "name" : "14",
                          "value": "14",
                        },
                        {
                          "name" : "15",
                          "value": "15",
                        },
                      ]
                    },
                    "type": "select",
                    "id": "minLength",

                  },
                  {
                    "label": "Minimum number of complex characters",
                    "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                    "optional": {
                      "option": [
                        {
                          "name" : "None",
                          "value": "",
                        },
                        {
                          "name" : "01",
                          "value": "1",
                        },
                        {
                          "name" : "02",
                          "value": "2",
                        },
                        {
                          "name" : "03",
                          "value": "3",
                        },

                        {
                          "name" : "04",
                          "value": "4",
                        },
                        {
                          "name" : "05",
                          "value": "5",
                        },
                      ]
                    },
                    "type": "select",
                    "id": "minComplexChars"
                  },
                  {
                    "label": "Maximum passcode age in days",
                    "tooltip": "Number of days after which a passcode must be changed",
                    "optional": {
                      "placeholder": "Should be in between 1-to-730 days or 0 for none",
                      "rules":{
                        "regex": "^(?:0|[1-9]|[1-9][1-9]|[0-6][0-9][0-9]|7[0-2][0-9]|730)$",
                        "validationMsg": "Should be in between 1-to-730 days or 0 for none",
                        "required": false
                      },
                    },
                    "type": "input",
                    "id": "maxPINAgeInDays"
                  },
                  {
                    "label": "Passcode history",
                    "tooltip": "Number of consequent unique passcodes to be used before reuse",
                    "optional": {
                      "placeholder": "Should be in between 1-to-50 passcodes or 0 for none",
                      "rules":{
                        "regex": "^(?:0|[1-9]|[1-4][0-9]|50)$",
                        "validationMsg": "Should be in between 1-to-50 passcodes or 0 for none",
                        "required": false
                      },
                    },
                    "type": "input",
                    "id": "pinHistory"
                  },
                  {
                    "label": "Maximum number of failed attempts",
                    "tooltip": "The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.",
                    "optional": {
                      "option": [
                        {
                          "name" : "None",
                          "value": "",
                        },
                        {
                          "name" : "03",
                          "value": "3",
                        },

                        {
                          "name" : "04",
                          "value": "4",
                        },
                        {
                          "name" : "05",
                          "value": "5",
                        },
                        {
                          "name" : "06",
                          "value": "6",
                        },
                        {
                          "name" : "07",
                          "value": "7",
                        },
                        {
                          "name" : "08",
                          "value": "8",
                        },
                        {
                          "name" : "09",
                          "value": "9",
                        },
                        {
                          "name" : "10",
                          "value": "10",
                        },
                      ]
                    },
                    "type": "select",
                    "id": "maxFailedAttempts"
                  },
                  {
                    "label": "Passcode policy for work profile",
                    "type": "title"
                  },
                  {
                    "label": "Enabled Work profile passcode",
                    "tooltip": "Enabled Work profile passcode.",
                    "optional": {
                      "checked": false,
                      "subPanel":
                          {
                            "panelItem": [
                              {
                                "label": "Allow Simple Value",
                                "tooltip": "Permits repeating, ascending and descending character sequences",
                                "optional": {
                                  "checked": true
                                },
                                "type": "checkbox",
                                "id": "passcodePolicyAllowSimpleWP"
                              },
                              {
                                "label": "Require alphanumeric value",
                                "tooltip": "Mandates to contain both letters and numbers",
                                "optional": {
                                  "checked": true
                                },
                                "type": "checkbox",
                                "id": "passcodePolicyRequireAlphanumericWP"
                              },
                              {
                                "label": "Minimum passcode length",
                                "tooltip": "Minimum number of characters allowed in a passcode",
                                "optional": {
                                  "option": [
                                    {
                                      "name" : "None",
                                      "value": "",
                                    },
                                    {
                                      "name" : "04",
                                      "value": "4",
                                    },
                                    {
                                      "name" : "05",
                                      "value": "5",
                                    },
                                    {
                                      "name" : "06",
                                      "value": "6",
                                    },
                                    {
                                      "name" : "07",
                                      "value": "7",
                                    },
                                    {
                                      "name" : "08",
                                      "value": "8",
                                    },
                                    {
                                      "name" : "09",
                                      "value": "9",
                                    },
                                    {
                                      "name" : "10",
                                      "value": "10",
                                    },
                                    {
                                      "name" : "11",
                                      "value": "11",
                                    },
                                    {
                                      "name" : "12",
                                      "value": "12",
                                    },
                                    {
                                      "name" : "13",
                                      "value": "13",
                                    },
                                    {
                                      "name" : "14",
                                      "value": "14",
                                    },
                                    {
                                      "name" : "15",
                                      "value": "15",
                                    },
                                  ]

                                },
                                "type": "select",
                                "id": "passcodePolicyMinLengthWP"
                              },
                              {
                                "label": "Minimum number of complex characters",
                                "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                                "optional": {
                                  "option": [
                                    {
                                      "name" : "None",
                                      "value": "",
                                    },
                                    {
                                      "name" : "01",
                                      "value": "1",
                                    },
                                    {
                                      "name" : "02",
                                      "value": "2",
                                    },
                                    {
                                      "name" : "03",
                                      "value": "3",
                                    },

                                    {
                                      "name" : "04",
                                      "value": "4",
                                    },
                                    {
                                      "name" : "05",
                                      "value": "5",
                                    },
                                  ]

                                },
                                "type": "select",
                                "id": "passcodePolicyMinComplexCharsWP"
                              },
                              {
                                "label": "Maximum passcode age in days",
                                "tooltip": "Number of days after which a passcode must be changed",
                                "optional": {
                                  "placeholder": "Should be in between 1-to-730 days or 0 for none",
                                  "rules":{
                                    "regex": "",
                                    "validationMsg": "",
                                    "required": false
                                  },
                                },
                                "type": "input",
                                "id": "passcodePolicyMaxPasscodeAgeInDaysWP"
                              },
                              {
                                "label": "Passcode history",
                                "tooltip": "Number of consequent unique passcodes to be used before reuse",
                                "optional": {
                                  "placeholder": "Should be in between 1-to-50 passcodes or 0 for none",
                                  "rules":{
                                    "regex": "",
                                    "validationMsg": "",
                                    "required": false
                                  },
                                },
                                "type": "input",
                                "id": "passcodePolicyPasscodeHistoryWP"
                              },
                              {
                                "label": "Maximum number of failed attempts",
                                "tooltip": "The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.",
                                "optional": {
                                  "option": [
                                    {
                                      "name" : "None",
                                      "value": "",
                                    },
                                    {
                                      "name" : "03",
                                      "value": "3",
                                    },

                                    {
                                      "name" : "04",
                                      "value": "4",
                                    },
                                    {
                                      "name" : "05",
                                      "value": "5",
                                    },
                                    {
                                      "name" : "06",
                                      "value": "6",
                                    },
                                    {
                                      "name" : "07",
                                      "value": "7",
                                    },
                                    {
                                      "name" : "08",
                                      "value": "8",
                                    },
                                    {
                                      "name" : "09",
                                      "value": "9",
                                    },
                                    {
                                      "name" : "10",
                                      "value": "10",
                                    },
                                  ]
                                },
                                "type": "select",
                                "id": "passcodePolicyMaxFailedAttemptsWP"
                              }
                            ],
                            "key": "workProfilePasscode",
                            "show": false
                          }
                    },
                    "type": "checkbox",
                    "id": "passcodePolicyWPExist",

                  }
                ],
                "key": "1",
                "show": true,
                "title": "Passcode Policy",
                "description": "Enforce a configured passcode policy on Android devices. Once this profile is applied," +
                    " the device owners won't be able to modify the password settings on their devices.",
              }
            },
          ]
        },
        {
          "name": "Restrictions",
          "panels": [{
            "panel":{
              "panelId": "CAMERA",
              "title": "Restrictions",
              "description": "This configurations can be used to restrict certain settings on an Android device. Once this configuration" +
                  " profile is installed on a device, corresponding users will not be able to modify these settings on their devices.",
              "panelItem": [
                {
                  "label": "Allow use of camera",
                  "tooltip": "Enables the usage of device camera",
                  "optional": {
                    "checked": true
                  },
                  "type": "checkbox",
                  "id": "CAMERA"
                },
                {
                  "label": "Below Restrictions are valid only when the Agent is work-profile owner or device owner.",
                  "type": "alert"
                },
                {
                  "label": "Disallow configuring VPN",
                  "tooltip": "Users are restricted from configuring VPN.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_VPN"
                },
                {
                  "label": "Disallow configuring app control",
                  "tooltip": "Restricts users from modifying applications in the device's settings or launchers.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_APPS_CONTROL"
                },
                {
                  "label": "Disallow cross profile copy paste",
                  "tooltip": "Device owners are restricted from copying items that are copied to the clipboard from the managed profile to the parent profile or vice-versa.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CROSS_PROFILE_COPY_PASTE"
                },
                {
                  "label": "Disallow debugging",
                  "tooltip": "Users are restricted from accessing debug logs.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_DEBUGGING_FEATURES"
                },
                {
                  "label": "Disallow install apps",
                  "tooltip": "Users are restricted from installing applications.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_INSTALL_APPS"
                },
                {
                  "label": "Disallow install from unknown sources",
                  "tooltip": "Users are restricted from installing applications from unknown origin.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_INSTALL_UNKNOWN_SOURCES"
                },
                {
                  "label": "Disallow modify accounts",
                  "tooltip": "Users are restricted from modifying user accounts.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_MODIFY_ACCOUNTS"
                },
                {
                  "label": "Disallow outgoing beam",
                  "tooltip": "Users are restricted from using NFC bump.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_OUTGOING_BEAM"
                },
                {
                  "label": "Disallow location sharing",
                  "tooltip": "Users are restricted from sharing their geo-location.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_SHARE_LOCATION"
                },
                {
                  "label": "Disallow uninstall apps",
                  "tooltip": "Users are restricted from uninstalling applications.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_UNINSTALL_APPS"
                },
                {
                  "label": "Disallow parent profile app linking",
                  "tooltip": "Allows apps in the parent profile to access or handle web links from the managed profile.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "ALLOW_PARENT_PROFILE_APP_LINKING"
                },
                {
                  "label": " Below restrictions will be applicable when the agent is the device owner and Android version 6.0 (Marshmallow) or higher.",
                  "type": "alert"
                },
                {
                  "label": "Disallow set wallpaper",
                  "tooltip": "Users are restricted from setting wallpapers.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_SET_WALLPAPER"
                },
                {
                  "label": "Disallow set user icon",
                  "tooltip": "Users are restricted from changing their icon.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_SET_USER_ICON"
                },
                {
                  "label": "Disallow remove managed profile",
                  "tooltip": "Users are restricted from removing the managed profile.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_REMOVE_MANAGEMENT_PROFILE"
                },
                {
                  "label": "Disallow autofill",
                  "tooltip": "Users are restricted from using autofill services.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_AUTOFILL"
                },
                {
                  "label": "Disallow bluetooth",
                  "tooltip": "Bluetooth is disallowed on the device.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_BLUETOOTH"
                },
                {
                  "label": "Disallow bluetooth sharing",
                  "tooltip": "Users are restricted from Bluetooth sharing on the device.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_BLUETOOTH_SHARING"
                },
                {
                  "label": "Disallow remove user",
                  "tooltip": "Users are restricted from removing user itself.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_CREDENTIALS"
                },
                {
                  "label": " Below Restrictions are valid only when the Agent is the device owner.",
                  "type": "alert"
                },
                {
                  "label": "Disallow SMS",
                  "tooltip": "Users are restricted from sending SMS messages.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_SMS"
                },
                {
                  "label": "Ensure verifying apps",
                  "tooltip": "Ensure app verification.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "ENSURE_VERIFY_APPS"
                },
                {
                  "label": "Enable auto timing",
                  "tooltip": "Enables the auto time feature that is in the device's Settings > Data & Time.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "AUTO_TIME"
                },
                {
                  "label": "Disable screen capture",
                  "tooltip": "Screen capturing would be disable.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "SET_SCREEN_CAPTURE_DISABLED"
                },
                {
                  "label": "Disallow volume adjust",
                  "tooltip": "Users are restricted from adjusting device volume.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_ADJUST_VOLUME"
                },
                {
                  "label": "Disallow cell broadcast",
                  "tooltip": "Users are restricted from configuring cell broadcast.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_CELL_BROADCASTS"
                },
                {
                  "label": "Disallow configuring bluetooth",
                  "tooltip": "Users are restricted from configuring bluetooth.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_BLUETOOTH"
                },
                {
                  "label": "Disallow configuring mobile networks",
                  "tooltip": "Users are restricted from configuring mobile network.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_MOBILE_NETWORKS"
                },
                {
                  "label": "Disallow configuring tethering",
                  "tooltip": "Users are restricted from configuring tethering.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_TETHERING"
                },
                {
                  "label": "Disallow configuring WIFI",
                  "tooltip": "Users are restricted from configuring Wifi.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_WIFI"
                },
                {
                  "label": "Disallow safe boot",
                  "tooltip": "Users are restricted to enter safe boot mode.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_SAFE_BOOT"
                },
                {
                  "label": "Disallow outgoing calls",
                  "tooltip": "Users are restricted from taking calls.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_OUTGOING_CALLS"
                },
                {
                  "label": "Disallow mount physical media",
                  "tooltip": "Users are restricted from mounting the device as physical media.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_MOUNT_PHYSICAL_MEDIA"
                },
                {
                  "label": "Disallow create window",
                  "tooltip": "Restricts device owners from opening new windows beside the app windows.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CREATE_WINDOWS"
                },
                {
                  "label": "Disallow factory reset",
                  "tooltip": "Users are restricted from performing factory reset.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_FACTORY_RESET"
                },
                {
                  "label": "Disallow remove user",
                  "tooltip": "Users are restricted from removing user.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_REMOVE_USER"
                },
                {
                  "label": "Disallow add user",
                  "tooltip": "Users are restricted from creating new users.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_ADD_USER"
                },
                {
                  "label": "Disallow network reset",
                  "tooltip": "Users are restricted from resetting network.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_NETWORK_RESET"
                },
                {
                  "label": "Disallow USB file transfer",
                  "tooltip": "Users are restricted from transferring files via USB.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_USB_FILE_TRANSFER"
                },
                {
                  "label": "Disallow unmute microphone",
                  "tooltip": "Users are restricted from unmuting the microphone.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_UNMUTE_MICROPHONE"
                },
                {
                  "label": "Below restrictions will be applied on devices with Android version 6.0 Marshmallow onwards only.",
                  "type": "alert"
                },
                {
                  "label": "Disable status bar",
                  "tooltip": "Checking this will disable the status bar.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "SET_STATUS_BAR_DISABLED"
                },
                {
                  "label": "Disallow data roaming",
                  "tooltip": "Users are restricted from using cellular data when roaming.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_DATA_ROAMING"
                },
                {
                  "label": "Enable device backup service",
                  "tooltip": "Device backup service wil be enabled.",
                  "optional": {
                    "checked": false
                  },
                  "type": "checkbox",
                  "id": "DISALLOW_CONFIG_CREDENTIALS"
                },

              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "Encryption Settings",
          "panels": [{
            "panel" : {
              "panelId": "ENCRYPT_STORAGE",
              "title": "Encryption Settings",
              "description": "This configuration can be used to encrypt data on an Android device, when the device" +
                  " is locked and make it readable when the passcode is entered. Once this configuration profile is installed on a device, " +
                  "corresponding users will not be able to modify these settings on their devices.",
              "panelItem": [
                {
                  "label": "Un-check following checkbox in case you do not need the device to be encrypted.",
                  "type": "paragraph",
                },
                {
                  "label": "Enable storage-encryption",
                  "tooltip": "Having this checked would enable Storage-encryption in the device.",
                  "optional": {
                    "checked": true
                  },
                  "type": "checkbox",
                  "id": "ENCRYPT_STORAGE"
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "Wi-Fi Settings",
          "panels": [{
            "panel": {
              "panelId": "WIFI",
              "title": "Wi-Fi Settings",
              "description": "This configurations can be used to configure Wi-Fi access on an Android device. " +
                  "Once this configuration profile is installed on a device, corresponding users will not be able to modify these settings on their devices.",
              "panelItem": [
                {
                  "label": "Please note that * sign represents required fields of data.",
                  "type": "paragraph",
                },
                {
                  "label": "Service Set Identifier (SSID) *",
                  "tooltip": "Identification of the wireless network to be configured.",
                  "optional": {
                    "placeholder": "Should be 1-to-30 characters long",
                    "rules":{
                      "regex": "^.{1,30}$",
                      "validationMsg": "Should be 1-to-30 characters long",
                      "required": false
                    },
                  },
                  "type": "input",
                  "id": "ssid"
                },
                {
                  "label": "Security *",
                  "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                  "optional": {
                    "option": [
                      {
                        "name" : "None",
                        "value": "",
                        "panelKey":"none"
                      },
                      {
                        "name" : "WEP",
                        "value": "wep",
                        "panelKey":"wep-wpa"
                      },
                      {
                        "name" : "WPA/WPA 2 PSK",
                        "value": "wpa",
                        "panelKey":"wep-wpa"
                      },
                      {
                        "name" : "802.1x EAP",
                        "value": "802eap",
                        "panelKey":"802eap"
                      },
                    ],
                    "subPanel": [
                      {
                        "panelItem": [
                          {
                            "type": "none",
                          },
                        ],
                        "key": "none",
                        "show": true
                      },
                      {
                        "panelItem": [
                          {
                            "label": "Password *",
                            "tooltip": "Password for the wireless network.",
                            "optional": {
                              "placeholder": "",
                              "rules":{
                                "regex": "",
                                "validationMsg": "",
                                "required": false
                              },
                            },
                            "type": "input",
                            "id": "password" //toDo add id
                          },
                        ],
                        "key": "wep",
                        "show": true
                      },
                      {
                        "panelItem": [
                          {
                            "label": "Password *",
                            "tooltip": "Password for the wireless network.",
                            "optional": {
                              "placeholder": "",
                              "rules":{
                                "regex": "",
                                "validationMsg": "",
                                "required": false
                              },
                            },
                            "type": "input",
                            "id": "password" //toDo add id
                          },
                        ],
                        "key": "wpa",
                        "show": true
                      },
                      {
                        "panelItem": [
                          {
                            "label": "EAP Method",
                            "tooltip": "EAP Method of the wireless network to be configured.",
                            "optional": {
                              "option": [
                                {
                                  "name" : "PEAP",
                                  "value": "peap",
                                },
                                {
                                  "name" : "TLS",
                                  "value": "tls",
                                },
                                {
                                  "name" : "TTLS",
                                  "value": "ttls",
                                },
                                {
                                  "name" : "PWD",
                                  "value": "pwd",
                                },
                                {
                                  "name" : "SIM",
                                  "value": "sim"
                                },
                                {
                                  "name" : "AKA",
                                  "value": "aka",
                                },
                              ],
                              "subPanel" : [
                                {
                                  "panelItem": [
                                    {
                                      "label": "Phase 2 Authentication",
                                      "tooltip": "Phase 2 authentication of the wireless network to be configured.",
                                      "optional": {
                                        "option": [
                                          {
                                            "name" : "None",
                                            "value": "",
                                          },
                                          {
                                            "name" : "PAP",
                                            "value": "pap",
                                          },
                                          {
                                            "name" : "MCHAP",
                                            "value": "mchap",
                                          },
                                          {
                                            "name" : "MCHAPV2",
                                            "value": "mchapv2",
                                          },
                                          {
                                            "name" : "GTC",
                                            "value": "gtc",
                                          },
                                        ]
                                      },
                                      "type": "select",
                                      "id": "phase2"
                                    },
                                    {
                                      "label": "Identify",
                                      "tooltip": "Identify of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "identity" //toDo add id
                                    },
                                    {
                                      "label": "Anonymous Identity",
                                      "tooltip": "Identity of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "anonymousIdentity" //toDo add id
                                    },
                                    {
                                      "label": "CA Certificate",
                                      "tooltip": "CA Certificate for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "upload",
                                      "id": "cacert" //toDo add id
                                    },
                                    {
                                      "label": "Password *",
                                      "tooltip": "Password for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "XXXXXXXXXXXX" //toDo add id
                                    },
                                  ],
                                  "key": "peap",
                                  "show": true
                                },
                                {
                                  "panelItem": [
                                    {
                                      "label": "Phase 2 Authentication",
                                      "tooltip": "Phase 2 authentication of the wireless network to be configured.",
                                      "optional": {
                                        "option": [
                                          {
                                            "name" : "None",
                                            "value": "",
                                          },
                                          {
                                            "name" : "PAP",
                                            "value": "pap",
                                          },
                                          {
                                            "name" : "MCHAP",
                                            "value": "mchap",
                                          },
                                          {
                                            "name" : "MCHAPV2",
                                            "value": "mchapv2",
                                          },
                                          {
                                            "name" : "GTC",
                                            "value": "gtc",
                                          },
                                        ]
                                      },
                                      "type": "select",
                                      "id": "phase2"
                                    },
                                    {
                                      "label": "Identify",
                                      "tooltip": "Identify of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "identity" //toDo add id
                                    },
                                    {
                                      "label": "Anonymous Identity",
                                      "tooltip": "Identity of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "anonymousIdentity" //toDo add id
                                    },
                                    {
                                      "label": "CA Certificate",
                                      "tooltip": "CA Certificate for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "upload",
                                      "id": "cacert" //toDo add id
                                    },
                                    {
                                      "label": "Password *",
                                      "tooltip": "Password for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "XXXXXXXXXXXX" //toDo add id
                                    },
                                  ],
                                  "key": "ttls",
                                  "show": true
                                },
                                {
                                  "panelItem": [
                                    {
                                      "label": "Identify",
                                      "tooltip": "Identify of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "identity" //toDo add id
                                    },
                                    {
                                      "label": "CA Certificate",
                                      "tooltip": "CA Certificate for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "upload",
                                      "id": "cacert" //toDo add id
                                    },
                                  ],
                                  "key": "tls",
                                  "show": true
                                },
                                {
                                  "panelItem": [
                                    {
                                      "label": "Identify",
                                      "tooltip": "Identify of the wireless network to be configured.",
                                      "optional": {
                                        "placeholder": "Should be 1 to 30 characters long",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "identity" //toDo add id
                                    },
                                    {
                                      "label": "Password *",
                                      "tooltip": "Password for the wireless network.",
                                      "optional": {
                                        "placeholder": "",
                                        "rules":{
                                          "regex": "",
                                          "validationMsg": "",
                                          "required": false
                                        },
                                      },
                                      "type": "input",
                                      "id": "XXXXXXXXXXXX" //toDo add id
                                    },
                                  ],
                                  "key": "pwd",
                                  "show": true
                                },
                                {
                                  "panelItem": [
                                    {
                                      "type": "none",
                                    },
                                  ],
                                  "key": "sim",
                                  "show": true
                                },
                                {
                                  "panelItem": [
                                    {
                                      "type": "none",
                                    },
                                  ],
                                  "key": "aka",
                                  "show": true
                                },
                              ]
                            },
                            "type": "select",
                            "id": "eap"
                          },
                        ],
                        "key": "802eap",
                        "show": true
                      },
                    ],
                  },
                  "type": "select",
                  "id": "type"
                },
              ],
              "key": "1",
              "show": true
            }
          },
          ]
        },
        {
          "name": "Global Proxy Settings",
          "panels": [
            {
              "panel":{
                "panelId": "GLOBAL_PROXY",
                "title": "Global Proxy Settings",
                "description": "This configurations can be used to set a network-independent global HTTP proxy on an " +
                    "Android device. Once this configuration profile is installed on a device, all the network traffic will " +
                    "be routed through the proxy server.",
                "panelItem": [
                  {
                    "label": "This profile requires the agent application to be the device owner.",
                    "type": "alert"
                  },
                  {
                    "label": "This proxy is only a recommendation and it is possible that some apps will ignore it.",
                    "type": "alert"
                  },
                  {
                    "label": "Please note that * sign represents required fields of data.",
                    "type": "paragraph",
                  },
                  {
                    "label": "Proxy Configuration Type",
                    "tooltip": "Select the configuration type.",
                    "optional": {
                      "initialValue": "MANUAL",
                      "radio": [
                        {
                          "name" : "Manual",
                          "value": "MANUAL",
                          "subPanel": [
                            {
                              "label": "Proxy Host *",
                              "tooltip": "Host name/IP address of the proxy server.",
                              "optional": {
                                "placeholder": "192.168.8.1",
                                "rules":{
                                  "regex": "^(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})$",
                                  "validationMsg": "Please enter valid IP address",
                                  "required": false
                                },
                              },
                              "type": "input",
                              "id": "proxyHost" //toDo add id
                            },
                            {
                              "label": "Proxy Port *",
                              "tooltip": "Target port for the proxy server.",
                              "optional": {
                                "placeholder": "Target port 0 - 65535",
                                "rules":{
                                  "regex": "^(?:0|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5])$",
                                  "validationMsg": "Please enter valid port",
                                  "required": false
                                },
                              },
                              "type": "input",
                              "id": "proxyPort" //toDo add id
                            },
                            {
                              "label": "Proxy Exclusion List",
                              "tooltip": "Add host names to this separated by commas to prevent them" +
                                  "from routing through the proxy server. The hostname entries can be wildcards such as" +
                                  "*.example.com",
                              "optional": {
                                "Placeholder": "Example : localhost, *.example.com",
                                "rules":{
                                  "regex": "",
                                  "validationMsg": "",
                                  "required": false
                                },
                              },
                              "type": "input",
                              "id": "proxyExclList" //toDo add id
                            },
                          ],
                        },
                        {
                          "name" : "Auto",
                          "value": "AUTO",
                          "subPanel": [
                            {
                              "label": "Proxy PAC File URL *",
                              "tooltip": "URL for the proxy auto config PAC script",
                              "optional": {
                                "placeholder": "http://exampleproxy.com/proxy.pac",
                                "rules":{
                                  "regex": "",
                                  "validationMsg": "",
                                  "required": false
                                },
                              },
                              "type": "input",
                              "id": "proxyPacUrl" //toDo add id
                            },
                          ],
                        },
                      ],
                    },
                    "type": "radioGroup",
                    "id": "proxyConfigType"  //toDo change id
                  },
                ],
                "key": "1",
                "show": true
              }
            },
          ]
        },
        {
          "name": "Virtual Private Network",
          "panels": [
            {
              "panel":{
                "panelId": "VPN",
                "title": "VPN Settings",
                "description": "Configure the OpenVPN settings on Android devices. In order to enable this, device needs to have \"OpenVPN for Android\" application installed.",
                "panelItem": [
                  {
                    "label": "OpenVPN Server Config file",
                    "tooltip": "OpenVPN configurations ovpn file.",
                    "optional": {
                      "placeholder": ""
                    },
                    "type": "upload",
                    "id": "XXXXXXXXXXvccvXX" //toDo add id
                  },

                ],
                "key": "cxcx1",
                "show": true
              }
            },
            {
              "panel": {
                "panelId": "VPN_ON",
                "title": "Always On VPN Settings",
                "description": "Configure an always-on VPN connection through a specific VPN client application.",
                "panelItem": [
                  {
                    "label": "Below configurations are valid only when the Agent is work-profile owner or device owner.",
                    "type": "alert"
                  },
                  {
                    "label": "VPN Client Application Package name* ",
                    "tooltip": "Package name of the VPN client application to be configured.",
                    "optional": {
                      "placeholder": "Should be a valid package name",
                      "rules":{
                        "regex": "",
                        "validationMsg": "",
                        "required": false
                      },
                    },
                    "type": "input",
                    "id": "packageName" //toDo add id
                  },
                ],
                "key": "1zxz",
                "show": true
              }
            }
          ]
        },
        {
          "name": "Certificates Install",
          "panels": [{
            "panel":{
              "panelId": "INSTALL_CERT",
              "title": "Certificate Install Settings",
              "description": "Configure the certificate install settings on Android devices.",
              "panelItem": [
                {
                  "label": "Added Certificate List",
                  "tooltip": "Add a certificate.",
                  "optional": {
                    "button": {
                      "id": "addCertificate",
                      "name": " Add Certificate"
                    },
                    "dataSource": "cert-list",
                    "columns": [
                      {
                        "name" : "Certificate name",
                        "key" : "CERT_NAME",
                        "type": "input",
                        "others": {
                          "placeholder":"Cert name",
                          "inputType": "text"
                        }
                      },
                      {
                        "name" : "Certificate File",
                        "key" : "CERT_CONTENT_VIEW",
                        "type": "upload",
                        "others": {
                        }
                      },
                    ]
                  },
                  "type": "inputTable",
                  "id": "CERT_LIST"
                },
              ],
              "key": "1",
              "show": true}
          }]
        },
        {
          "name": "Work-Profile Configurations",
          "panels": [{
            "panel": {
              "panelId": "WORK_PROFILE",
              "title": "Work-Profile Configurations",
              "description": "Configure these settings to manage the applications in the work profile.",
              "panelItem": [
                {
                  "label": "Profile name",
                  "tooltip": "name of the Work-Profile created by IOT Server Agent.",
                  "optional": {
                    "placeholder": "",
                    "rules":{
                      "regex": "",
                      "validationMsg": "",
                      "required": false
                    },
                  },
                  "type": "input",
                  "id": "XXXXXXXXczxczxXXXX" //toDo add id
                },
                {
                  "label": "Enable System Apps",
                  "tooltip": "The set of system apps needed to be added to the work-profile.",
                  "optional": {
                    "placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                    "row": 4
                  },
                  "type": "textArea",
                  "id": "ENCRYPT_STxcxORAGcxcxcE"
                },
                {
                  "label": "Hide System Apps",
                  "tooltip": "The set of system apps needed to be hide in the work-profile.",
                  "optional": {
                    "placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                    "row": 4
                  },
                  "type": "textArea",
                  "id": "ENCRYPccxT_STORAGcxcxcE"
                },
                {
                  "label": "Unhide System Apps",
                  "tooltip": "The set of system apps needed to be unhide in the work-profile.",
                  "optional": {
                    "placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                    "row": 4
                  },
                  "type": "textArea",
                  "id": "ENCRYPccxT_STORAGcxcxcE"
                },
                {
                  "label": "Enable Google PlayStore Apps",
                  "tooltip": "The set of apps needed to be installed from Google PlayStore to work-profile.",
                  "optional": {
                    "placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                    "row": 4
                  },
                  "type": "textArea",
                  "id": "ENCRYPccxT_STORAGcxcxcE"
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "COSU Profile Configurations",
          "panels": [{
            "panel": {
              "panelId": "COSU_PROFILE",
              "title": "COSU Profile Configurations",
              "description": "This policy can be used to configure the profile of COSU Devices.",
              "panelItem": [
                {
                  "label": "Restrict Device Operation Time",
                  "tooltip": "",
                  "optional": {
                    "checked": false,
                    "subPanel":
                        {
                          "panelItem": [
                            {
                              "label": "Start Time",
                              "tooltip": "Start time for the device",
                              "optional": {
                                "option": [
                                  {
                                    "name" : "12:00 Midnight",
                                    "value": "1440",
                                  },
                                  {
                                    "name" : "12:30 AM",
                                    "value": "30",
                                  },
                                  {
                                    "name" : "01:00 AM",
                                    "value": "60",
                                  },
                                  {
                                    "name" : "01:30 AM",
                                    "value": "90",
                                  },
                                  {
                                    "name" : "02:00 AM",
                                    "value": "120",
                                  },
                                  {
                                    "name" : "02:30 AM",
                                    "value": "150",
                                  },
                                  {
                                    "name" : "03:00 AM",
                                    "value": "180",
                                  },
                                  {
                                    "name" : "03:30 AM",
                                    "value": "210",
                                  },
                                  {
                                    "name" : "04:00 AM",
                                    "value": "240",
                                  },
                                  {
                                    "name" : "04:30 AM",
                                    "value": "270",
                                  },
                                  {
                                    "name" : "05:00 AM",
                                    "value": "300",
                                  },
                                  {
                                    "name" : "05:30 AM",
                                    "value": "330",
                                  },
                                  {
                                    "name" : "06:00 AM",
                                    "value": "360",
                                  },
                                  {
                                    "name" : "06:30 AM",
                                    "value": "390",
                                  },
                                  {
                                    "name" : "07:00 AM",
                                    "value": "420",
                                  },
                                  {
                                    "name" : "07:30 AM",
                                    "value": "450",
                                  },
                                  {
                                    "name" : "08:00 AM",
                                    "value": "480",
                                  },
                                  {
                                    "name" : "08:30 AM",
                                    "value": "510",
                                  },
                                  {
                                    "name" : "09:00 AM",
                                    "value": "540",
                                  },
                                  {
                                    "name" : "09:30 AM",
                                    "value": "570",
                                  },
                                  {
                                    "name" : "10:00 AM",
                                    "value": "600",
                                  },
                                  {
                                    "name" : "10:30 AM",
                                    "value": "630",
                                  },
                                  {
                                    "name" : "11:00 AM",
                                    "value": "660",
                                  },
                                  {
                                    "name" : "11:30 AM",
                                    "value": "690",
                                  },
                                  {
                                    "name" : "12:00 noon",
                                    "value": "720",
                                  },
                                  {
                                    "name" : "12:30 PM",
                                    "value": "750",
                                  },
                                  {
                                    "name" : "01:00 PM",
                                    "value": "780",
                                  },
                                  {
                                    "name" : "01:30 PM",
                                    "value": "810",
                                  },
                                  {
                                    "name" : "02:00 PM",
                                    "value": "840",
                                  },
                                  {
                                    "name" : "02:30 PM",
                                    "value": "870",
                                  },
                                  {
                                    "name" : "03:00 PM",
                                    "value": "900",
                                  },
                                  {
                                    "name" : "03:30 PM",
                                    "value": "930",
                                  },
                                  {
                                    "name" : "04:00 PM",
                                    "value": "960",
                                  },
                                  {
                                    "name" : "04:30 PM",
                                    "value": "990",
                                  },
                                  {
                                    "name" : "05:00 PM",
                                    "value": "1020",
                                  },
                                  {
                                    "name" : "05:30 PM",
                                    "value": "1050",
                                  },
                                  {
                                    "name" : "06:00 PM",
                                    "value": "1080",
                                  },
                                  {
                                    "name" : "06:30 PM",
                                    "value": "1110",
                                  },
                                  {
                                    "name" : "07:00 PM",
                                    "value": "1140",
                                  },
                                  {
                                    "name" : "07:30 PM",
                                    "value": "1170",
                                  },
                                  {
                                    "name" : "08:00 PM",
                                    "value": "1200",
                                  },
                                  {
                                    "name" : "08:30 PM",
                                    "value": "1230",
                                  },
                                  {
                                    "name" : "09:00 PM",
                                    "value": "1260",
                                  },
                                  {
                                    "name" : "09:30 PM",
                                    "value": "1290",
                                  },
                                  {
                                    "name" : "10:00 PM",
                                    "value": "1320",
                                  },
                                  {
                                    "name" : "10:30 PM",
                                    "value": "1350",
                                  },
                                  {
                                    "name" : "11:00 PM",
                                    "value": "1380",
                                  },
                                  {
                                    "name" : "11:30 PM",
                                    "value": "1410",
                                  },
                                ]

                              },
                              "type": "select",
                              "id": "STARTTIME"
                            },
                            {
                              "label": "End Time",
                              "tooltip": "Lock-down time for the device",
                              "optional": {
                                "option": [
                                  {
                                    "name" : "12:00 Midnight",
                                    "value": "1440",
                                  },
                                  {
                                    "name" : "12:30 AM",
                                    "value": "30",
                                  },
                                  {
                                    "name" : "01:00 AM",
                                    "value": "60",
                                  },
                                  {
                                    "name" : "01:30 AM",
                                    "value": "90",
                                  },
                                  {
                                    "name" : "02:00 AM",
                                    "value": "120",
                                  },
                                  {
                                    "name" : "02:30 AM",
                                    "value": "150",
                                  },
                                  {
                                    "name" : "03:00 AM",
                                    "value": "180",
                                  },
                                  {
                                    "name" : "03:30 AM",
                                    "value": "210",
                                  },
                                  {
                                    "name" : "04:00 AM",
                                    "value": "240",
                                  },
                                  {
                                    "name" : "04:30 AM",
                                    "value": "270",
                                  },
                                  {
                                    "name" : "05:00 AM",
                                    "value": "300",
                                  },
                                  {
                                    "name" : "05:30 AM",
                                    "value": "330",
                                  },
                                  {
                                    "name" : "06:00 AM",
                                    "value": "360",
                                  },
                                  {
                                    "name" : "06:30 AM",
                                    "value": "390",
                                  },
                                  {
                                    "name" : "07:00 AM",
                                    "value": "420",
                                  },
                                  {
                                    "name" : "07:30 AM",
                                    "value": "450",
                                  },
                                  {
                                    "name" : "08:00 AM",
                                    "value": "480",
                                  },
                                  {
                                    "name" : "08:30 AM",
                                    "value": "510",
                                  },
                                  {
                                    "name" : "09:00 AM",
                                    "value": "540",
                                  },
                                  {
                                    "name" : "09:30 AM",
                                    "value": "570",
                                  },
                                  {
                                    "name" : "10:00 AM",
                                    "value": "600",
                                  },
                                  {
                                    "name" : "10:30 AM",
                                    "value": "630",
                                  },
                                  {
                                    "name" : "11:00 AM",
                                    "value": "660",
                                  },
                                  {
                                    "name" : "11:30 AM",
                                    "value": "690",
                                  },
                                  {
                                    "name" : "12:00 noon",
                                    "value": "720",
                                  },
                                  {
                                    "name" : "12:30 PM",
                                    "value": "750",
                                  },
                                  {
                                    "name" : "01:00 PM",
                                    "value": "780",
                                  },
                                  {
                                    "name" : "01:30 PM",
                                    "value": "810",
                                  },
                                  {
                                    "name" : "02:00 PM",
                                    "value": "840",
                                  },
                                  {
                                    "name" : "02:30 PM",
                                    "value": "870",
                                  },
                                  {
                                    "name" : "03:00 PM",
                                    "value": "900",
                                  },
                                  {
                                    "name" : "03:30 PM",
                                    "value": "930",
                                  },
                                  {
                                    "name" : "04:00 PM",
                                    "value": "960",
                                  },
                                  {
                                    "name" : "04:30 PM",
                                    "value": "990",
                                  },
                                  {
                                    "name" : "05:00 PM",
                                    "value": "1020",
                                  },
                                  {
                                    "name" : "05:30 PM",
                                    "value": "1050",
                                  },
                                  {
                                    "name" : "06:00 PM",
                                    "value": "1080",
                                  },
                                  {
                                    "name" : "06:30 PM",
                                    "value": "1110",
                                  },
                                  {
                                    "name" : "07:00 PM",
                                    "value": "1140",
                                  },
                                  {
                                    "name" : "07:30 PM",
                                    "value": "1170",
                                  },
                                  {
                                    "name" : "08:00 PM",
                                    "value": "1200",
                                  },
                                  {
                                    "name" : "08:30 PM",
                                    "value": "1230",
                                  },
                                  {
                                    "name" : "09:00 PM",
                                    "value": "1260",
                                  },
                                  {
                                    "name" : "09:30 PM",
                                    "value": "1290",
                                  },
                                  {
                                    "name" : "10:00 PM",
                                    "value": "1320",
                                  },
                                  {
                                    "name" : "10:30 PM",
                                    "value": "1350",
                                  },
                                  {
                                    "name" : "11:00 PM",
                                    "value": "1380",
                                  },
                                  {
                                    "name" : "11:30 PM",
                                    "value": "1410",
                                  },
                                ]

                              },
                              "type": "select",
                              "id": "ENDTIME"
                            },
                            {
                              "label": "Device will be operable only during the above period.",
                              "type": "alert"
                            },
                          ],
                          "key": "RestrictDeviceOperationTime",
                          "show": true
                        }
                  },
                  "type": "checkbox",
                  "id": "ENCRYPT_STOcxcxcRAGhftyE"
                },
                {
                  "label": "Device Global Configuration",
                  "tooltip": "",
                  "optional": {
                    "checked": false,
                    "subPanel": {
                      "panelItem": [
                        {
                          "label": "Launcher background image",
                          "tooltip": "This is the image that will be displayed in kiosk background.",
                          "optional": {
                            "Placeholder": "Should be a valid URL of jpg or jpeg or png",
                            "rules":{
                              "regex": "",
                              "validationMsg": "",
                              "required": false
                            },
                          },
                          "type": "input",
                          "id": "XXXXXXXsdsdXXXXX" //toDo add id
                        },
                        {
                          "label": "Company logo to display",
                          "tooltip": "Company logo to display",
                          "optional": {
                            "Placeholder": "Should be a valid URL ending with .jpg, .png, .jpeg",
                            "rules":{
                              "regex": "",
                              "validationMsg": "",
                              "required": false
                            },
                          },
                          "type": "input",
                          "id": "XXXXXXXsdsdXXXXX" //toDo add id
                        },
                        {
                          "label": "Company name",
                          "tooltip": "Company name",
                          "optional": {
                            "Placeholder": "name to appear on the agent",
                            "rules":{
                              "regex": "",
                              "validationMsg": "",
                              "required": false
                            },
                          },
                          "type": "input",
                          "id": "XXXXXXXsdsdXXXXX" //toDo add id
                        },
                        {
                          "label": "Is single application mode",
                          "tooltip": "Is single application mode",
                          "optional": {
                            "checked": false,
                            "subPanel": {
                              "panelItem": [
                                {
                                  "label": "Selected initial app in Enrollment Application Install" +
                                      " policy config will be selected for single application mode.",
                                  "type": "alert",
                                },
                                {
                                  "label": "Is application built for Kiosk",
                                  "tooltip": "Is single mode app built for Kisosk. Enable if lock task method is called in the " +
                                      "application",
                                  "optional": {
                                    "checked": true,
                                  },
                                  "type": "checkbox",
                                  "id": "Efdgdg"
                                },
                              ],
                              "key": "SingleAppMode",
                              "show": true
                            },

                          },
                          "type": "checkbox",
                          "id": "ENCRsdfsdfdAGE"
                        },
                        {
                          "label": "Is idle media enabled ",
                          "tooltip": "Is idle media enabled ",
                          "optional": {
                            "checked": false,
                            "subPanel": {
                              "panelItem": [
                                {
                                  "label": "Media to display while idle",
                                  "tooltip": "Media to display while the device is idle",
                                  "optional": {
                                    "Placeholder": "Should be a valid URL ending with .jpg, .png, .jpeg, .mp4, .3gp, .wmv, .mkv",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "XXXXXXXsdsdXXXXX" //toDo add id
                                },
                                {
                                  "label": "Idle graphic begin after(seconds)",
                                  "tooltip": "Idle graphic begin after the defined seconds",
                                  "optional": {
                                    "Placeholder": "Idle timeout in seconds",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "XXXXXXXsdsdXXXXX" //toDo add id
                                },
                              ],
                              "key": "idleMediaEnabled",
                              "show": true
                            }
                          },
                          "type": "checkbox",
                          "id": "ENCsdsdsORAGE"
                        },
                        {
                          "label": "Is multi-user device",
                          "tooltip": "Is multi-user device.",
                          "optional": {
                            "checked": false,
                            "subPanel":{
                              "panelItem": [
                                {
                                  "label": "Is login needed for user switch",
                                  "tooltip": "Permits repeating, ascending and descending character sequences",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isLoginRequired"
                                },
                                {
                                  "label": " Provide comma separated package name or web clip details for applications.\n" +
                                      "eg: com.google.android.apps.maps, {\"identity\":\"http:entgra.io/\",\"title\":\"entgra-webclip\"}",
                                  "type": "alert",
                                },
                                {
                                  "label": "Primary User Apps ",
                                  "tooltip": "Primary User is the user to which the device is enrolled",
                                  "optional": {
                                    "Placeholder": "Applications",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "primaryUserApps" //toDo add id
                                },
                                {
                                  "label": "Add User Applications",
                                  "tooltip": "Add User Applications.",
                                  "optional": {
                                    "button": {
                                      "id": "addUserApps",
                                      "name": " Add User Apps"
                                    },
                                    "dataSource": "cosu-user-app-config",
                                    "columns": [
                                      {
                                        "name" : "User",
                                        "key" : "username",
                                        "type": "input",
                                        "others": {
                                          "placeholder":"Username",
                                          "inputType": "text"
                                        }
                                      },
                                      {
                                        "name" : "Applications",
                                        "key" : "visibleAppList",
                                        "type": "input",
                                        "others": {
                                          "placeholder":"",
                                          "inputType": "text"
                                        }
                                      },
                                    ]
                                  },
                                  "type": "inputTable",
                                  "id": "ENCRYcbchdPT_STORAGE"
                                },
                              ],
                              "key": "idleMediaEnabled",
                              "show": true
                            }
                          },
                          "type": "checkbox",
                          "id": "isMultiUserDevice"
                        },
                        {
                          "label": "Device display orientation",
                          "tooltip": "Device display orientation",
                          "optional": {
                            "option": [
                              {
                                "name": "Auto",
                                "value": "auto"
                              },
                              {
                                "name": "Portrait",
                                "value": "portrait"
                              },
                              {
                                "name": "Landscape",
                                "value": "landscape"
                              },
                            ]
                          },
                          "type": "select",
                          "id": "minCompzzdlexChars"
                        },
                        {
                          "label": "Enable Browser Properties",
                          "tooltip": "Browser Properties",
                          "optional": {
                            "checked": false,
                            "subPanel": {
                              "panelItem": [
                                {
                                  "label": "Primary URL ",
                                  "tooltip": "Primary URL",
                                  "optional": {
                                    "Placeholder": "Should be a valid URL",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "primaryURL" //toDo add id
                                },
                                {
                                  "label": "Enable browser address bar",
                                  "tooltip": "Enables address bar of the browser",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isAddressBarEnabled"
                                },
                                {
                                  "label": "Is allow to go back on a page",
                                  "tooltip": "Allow to go back in a page",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "showBackController"
                                },
                                {
                                  "label": "Is it allowed to go forward in browser",
                                  "tooltip": "Is it allowed to go forward in a web page",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isForwardControllerEnabled"
                                },
                                {
                                  "label": "Is home button enabled",
                                  "tooltip": "Is home button enabled",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isHomeButtonEnabled"
                                },
                                {
                                  "label": "Is page reload enabled",
                                  "tooltip": "Is page reload enabled",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isReloadEnabled"
                                },
                                {
                                  "label": "Only allowed to visit the primary url",
                                  "tooltip": "Only allowed to visit the primary url",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "lockToPrimaryURL"
                                },
                                {
                                  "label": "Is javascript enabled",
                                  "tooltip": "Is javascript enabled",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isJavascriptEnabled"
                                },
                                {
                                  "label": "Is copying to visit the primary url",
                                  "tooltip": "Is copying to visit the primary url",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isTextCopyEnabled"
                                },
                                {
                                  "label": "Is downloading files enabled",
                                  "tooltip": "Is downloading files enabled",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isDownloadsEnabled"
                                },
                                {
                                  "label": "Is Kiosk limited to one webapp",
                                  "tooltip": "Is Kiosk limited to one webapp.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isLockedToBrowser"
                                },
                                {
                                  "label": "Is form auto-fill enabled",
                                  "tooltip": "Is form auto-fill enabled.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isFormAutoFillEnabled"
                                },
                                {
                                  "label": "Is content access enabled",
                                  "tooltip": "Enables or disable content URL access within WebView. Content URL" +
                                      "access allows WebView to load content from a content provider installed in the system.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isContentAccessEnabled"
                                },
                                {
                                  "label": "Is file access allowed",
                                  "tooltip": "Sets whether javascript running in the context of a file schema URL should be" +
                                      "allowed to access content from other file scheme URLs.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isFileAccessAllowed"
                                },
                                {
                                  "label": "Is allowed universal access from file URLs",
                                  "tooltip": "Sets whether JavaScript running in the context of a file scheme URL should be allowed" +
                                      "to access content from any origin",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isAllowedUniversalAccessFromFileURLs"
                                },
                                {
                                  "label": "Is application cache enabled",
                                  "tooltip": "Is application cache enabled",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isAppCacheEnabled"
                                },
                                {
                                  "label": "Application cache file path",
                                  "tooltip": "Sets the path to the Application Cache files. In order for the Application Caches API" +
                                      "to be enabled, this method must be called with a path to which the application can write",
                                  "optional": {
                                    "Placeholder": "Should be a valid path",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "appCachePath"
                                },
                                {
                                  "label": "Application cache mode",
                                  "tooltip": "Overrides the way the cache is used. The way the cache is used is based on the navigation" +
                                      "type. For a normal page load, the cache is checked and content is re-validated as needed. " +
                                      "When navigating back, content is not revalidated, instead the content is just retrieved from the cache." +
                                      "This method allows the client to override this behavior by specifying one of LOAD_DEFAULT," +
                                      "LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE or LOAD_CACHE_ONLY",
                                  "optional": {
                                    "option": [
                                      {
                                        "name" : "LOAD_DEFAULT",
                                        "value" : "-1",
                                      },
                                      {
                                        "name" : "LOAD_CACHE_ELSE_NETWORK",
                                        "value" : "1",
                                      },
                                      {
                                        "name" : "LOAD_NO_CACHE",
                                        "value" : "2",
                                      },
                                      {
                                        "name" : "LOAD_CACHE_ONLY",
                                        "value" : "3",
                                      },
                                    ]
                                  },
                                  "type": "select",
                                  "id": "cacheMode"
                                },
                                {
                                  "label": "Should load images",
                                  "tooltip": "Sets whether the browser should load image resources (through network and cached)." +
                                      "Note that this method controls loading of all images, including those embedded using the data URI" +
                                      "scheme.",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isLoadsImagesAutomatically"
                                },
                                {
                                  "label": "Block image loads via network",
                                  "tooltip": "Sets whether the browser should not load image resources from the network" +
                                      "(resources accessed via http and https URI schemes)",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isBlockNetworkImage"
                                },
                                {
                                  "label": "Block all resource loads from network",
                                  "tooltip": "Sets whether the browser should not load any resources from the network.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isBlockNetworkLoads"
                                },
                                {
                                  "label": "Support zooming",
                                  "tooltip": "Sets whether the browser should support zooming using its on-screen zoom" +
                                      "controls and gestures",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isSupportZoomEnabled"
                                },
                                {
                                  "label": "Show on-screen zoom controllers",
                                  "tooltip": "Sets whether the browser should display on-screen zoom controls. Gesture based controllers" +
                                      "are still available",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isDisplayZoomControls"
                                },
                                {
                                  "label": "Text zoom percentage",
                                  "tooltip": "Sets the text zoom of the page in percent",
                                  "optional": {
                                    "Placeholder": "Should be a positive number",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "textZoom"
                                },
                                {
                                  "label": "Default font size",
                                  "tooltip": "Sets the default font size",
                                  "optional": {
                                    "Placeholder": "Should be a positive number between 1 and 72",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "defaultFontSize"
                                },
                                {
                                  "label": "Default text encoding name",
                                  "tooltip": "Sets the default text encoding name to use when decoding html pages",
                                  "optional": {
                                    "Placeholder": "Should a valid text encoding",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "defaultTextEncodingName"
                                },
                                {
                                  "label": "Is database storage API enabled",
                                  "tooltip": "Sets whether the database storage API is enabled.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isDatabaseEnabled"
                                },
                                {
                                  "label": "Is DOM storage API enabled",
                                  "tooltip": "Sets whether the DOM storage API is enabled.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isDomStorageEnabled"
                                },
                                {
                                  "label": "Is Geo-location enabled",
                                  "tooltip": "Sets whether Geo-location API is enabled.",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "geolocationEnabled"
                                },
                                {
                                  "label": "Can JavaScript open windows",
                                  "tooltip": "JavaScript can open window automatically or not. This applies to the JavaScript" +
                                      "function window.open()",
                                  "optional": {
                                    "checked": false
                                  },
                                  "type": "checkbox",
                                  "id": "isJavaScriptCanOpenWindowsAutomatically"
                                },
                                {
                                  "label": "Does media playback requires user consent",
                                  "tooltip": "Sets whether the browser requires a user gesture to play media. If false, the browser" +
                                      "can play media without user consent",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isMediaPlaybackRequiresUserGesture"
                                },
                                {
                                  "label": "Is safe browsing enabled",
                                  "tooltip": "Sets whether safe browsing in enabled. Safe browsing allows browser to protect against malware and" +
                                      " phishing attacks by verifying the links.",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isSafeBrowsingEnabled"
                                },
                                {
                                  "label": "Use wide view port",
                                  "tooltip": "Sets whether the browser should enable support for the viewport HTML meta tag or should" +
                                      "use a wide viewport. When the value of the setting is false, the layout width is always set to the " +
                                      "width of the browser control in  device-independent (CSS) pixels. When the value is true and the" +
                                      "page contains the viewport meta tag, the value of the width specified in th tag is used. If the page" +
                                      "does not contain the tag or does not provide a width, then a wide viewport will be used",
                                  "optional": {
                                    "checked": true
                                  },
                                  "type": "checkbox",
                                  "id": "isUseWideViewPort"
                                },
                                {
                                  "label": "Browser user agent string",
                                  "tooltip": "Sets the WebView's user-agent string",
                                  "optional": {
                                    "Placeholder": "Should be a valid user agent string",
                                    "rules":{
                                      "regex": "",
                                      "validationMsg": "",
                                      "required": false
                                    },
                                  },
                                  "type": "input",
                                  "id": "userAgentString" //toDo add id
                                },
                                {
                                  "label": "Mixed content mode",
                                  "tooltip": "Configures the browser's behavior when a secure origin attempts to load a resource" +
                                      "from an insecure origin",
                                  "optional": {
                                    "option": [
                                      {
                                        "name": "MIXED_CONTENT_ALWAYS_ALLOW",
                                        "value": "0",
                                      },
                                      {
                                        "name": "MIXED_CONTENT_NEVER_ALLOW",
                                        "value": "1",
                                      },
                                      {
                                        "name": "MIXED_CONTENT_COMPATIBILITY_MODE",
                                        "value": "2",
                                      },
                                    ]
                                  },
                                  "type": "select",
                                  "id": "mixedContentMode"
                                },

                              ],
                              "key": "idleMediaEnabled",
                              "show": true
                            }
                          },
                          "type": "checkbox",
                          "id": "ENCsdsdcxzcxsORAGE"
                        },
                        {
                          "label": "Global configurations related to device.",
                          "type": "alert",
                        },
                      ],
                      "key": "DeviceGlobalConfiguration",
                      "show": true
                    },
                  },
                  "type": "checkbox",
                  "id": "ENCRYPT_STOxcxcRAGE"
                },
              ],
              "key": "1",
              "show": true
            }
          },
          ]
        },
        {
          "name": "Application Restrictions",
          "panels": [{
            "panel": {
              "panelId": "APP_RESTRICTION",
              "title": "Application Restriction Setting",
              "description": "This configuration can be used to encrypt data on an Android device, when the device is locked and make it " +
                  "readable when the passcode is entered. Once this configuration profile is installed on a device, corresponding users" +
                  " will not be able to modify these settings on their devices.",
              "panelItem": [
                {
                  "label": "Select type",
                  "tooltip": "Select a type to proceed",
                  "optional": {
                    "option": [
                      {
                        "name": "None",
                        "value": "",
                      },
                      {
                        "name": "Black List",
                        "value": "black-list",
                      },
                      {
                        "name": "White List",
                        "value": "white-list",
                      },
                    ]
                  },
                  "type": "select",
                  "id": "restrictionType"
                },
                {
                  "label": "Restricted Application List",
                  "tooltip": "Add an application to restrict.",
                  "optional": {
                    "button": {
                      "id": "addApplication",
                      "name": " Add Application"
                    },
                    "dataSource": "restricted-applications",
                    "columns": [
                      {
                        "name" : "Application name/Description",
                        "key" : "appName",
                        "type": "input",
                        "others": {
                          "placeholder":"Gmail",
                          "inputType": "email"
                        }
                      },
                      {
                        "name" : "Package name",
                        "key" : "packageName",
                        "type": "input",
                        "others": {
                          "placeholder":"com.google.android.gm",
                          "inputType": "text"
                        }
                      },
                    ]
                  },
                  "type": "inputTable",
                  "id": "RestrictedApplicationList"
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "Runtime Permission Policy (COSU)",
          "panels": [{
            "panel": {
              "panelId": "RUNTIME_PERMISSION_POLICY",
              "title": "Runtime Permission Policy (COSU / Work Profile)",
              "description": "This configuration can be used to set a runtime permission policy to an Android Device.",
              "panelItem": [
                {
                  "label": "Set default runtime permission",
                  "tooltip": "When an app requests a runtime permission this enforces whether the user needs" +
                      " to prompted or the permission either automatically granted or denied",
                  "optional": {
                    "option": [
                      {
                        "name": "NONE",
                        "value": "",
                      },
                      {
                        "name": "PROMPT USER",
                        "value": "0",
                      },
                      {
                        "name": "AUTO GRANT",
                        "value": "1",
                      },
                      {
                        "name": "AUTO DENY",
                        "value": "2",
                      },
                    ]
                  },
                  "type": "select",
                  "id": "defaultPermissionType"
                },
                {
                  "label": "Set app-specific runtime permissions",
                  "tooltip": "Add an application and set permission policy for a specific permission it need.",
                  "optional": {
                    "button": {
                      "id": "addApplication-runtimePermission",
                      "name": " Add Application"
                    },
                    "dataSource": "permittedApplications",
                    "columns": [
                      {
                        "name" : "Application",
                        "key" : "appName",
                        "type": "input",
                        "others": {
                          "placeholder":"Android Pay",
                          "inputType": "text"
                        }
                      },
                      {
                        "name" : "Package name",
                        "key" : "packageName",
                        "type": "input",
                        "others": {
                          "placeholder":"com.google.android.pay",
                          "inputType": "text"
                        }
                      },
                      {
                        "name" : "Permission name",
                        "key" : "permissionName",
                        "type": "input",
                        "others": {
                          "placeholder":"android.permission.NFC",
                          "inputType": "text"
                        }
                      },
                      {
                        "name" : "Permission Type",
                        "key" : "permissionType",
                        "type": "select",
                        "others": {
                          "initialDataIndex": "1",
                          "options":[
                            {
                              "value":"PROMPT USER",
                              "key": "0"
                            },
                            {
                              "value": "AUTO GRANT",
                              "key": "1"
                            },
                            {
                              "value": "AUTO DENY",
                              "key": "2"
                            },
                          ],
                        }
                      },
                    ]
                  },
                  "type": "inputTable",
                  "id": "restricted-applications"
                },
                {
                  "label": "Already granted or denied permissions are not affected by this policy.",
                  "type": "alert",
                },
                {
                  "label": "Permissions can be granted or revoked only for applications built with a Target SDK Version of Android Marshmallow or later.",
                  "type": "alert",
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "System Update Policy (COSU)",
          "panels": [{
            "panel":{
              "panelId": "SYSTEM_UPDATE_POLICY",
              "title": "System Update Policy (COSU)",
              "description": "This configuration can be used to set a passcode policy to an Android Device. Once this" +
                  " configuration profile is installed on a device, corresponding users will not be able to modify " +
                  "these settings on their devices.",
              "panelItem": [
                {
                  "label": "System Update",
                  "tooltip": "Type of the System Update to be set by the Device Owner.",
                  "optional": {
                    "initialValue": "automatic",
                    "radio": [
                      {
                        "name": "Automatic",
                        "value": "automatic",
                        "subPanel": [{
                          "type": "none",
                        }]
                      },
                      {
                        "name": "Postpone",
                        "value": "postpone",
                        "subPanel": [{
                          "type": "none",
                        }]
                      },
                      {
                        "name": "Window",
                        "value": "window",
                        "subPanel": [
                          {
                            "label": "Below configuration of start time and end time are valid only when window option is selected.",
                            "type": "alert",
                          },
                          {
                            "label": "Start Time",
                            "tooltip": "Window start time for system update",
                            "optional": {
                              "option": [
                                {
                                  "name" : "12:00 AM",
                                  "value": "1440",
                                },
                                {
                                  "name" : "01:00 AM",
                                  "value": "60",
                                },
                                {
                                  "name" : "02:00 AM",
                                  "value": "120",
                                },
                                {
                                  "name" : "03:00 AM",
                                  "value": "180",
                                },
                                {
                                  "name" : "04:00 AM",
                                  "value": "240",
                                },
                                {
                                  "name" : "05:00 AM",
                                  "value": "300",
                                },
                                {
                                  "name" : "06:00 AM",
                                  "value": "360",
                                },
                                {
                                  "name" : "07:00 AM",
                                  "value": "420",
                                },
                                {
                                  "name" : "08:00 AM",
                                  "value": "480",
                                },
                                {
                                  "name" : "09:00 AM",
                                  "value": "540",
                                },
                                {
                                  "name" : "10:00 AM",
                                  "value": "600",
                                },
                                {
                                  "name" : "11:00 AM",
                                  "value": "660",
                                },
                                {
                                  "name" : "12:00 noon",
                                  "value": "720",
                                },
                                {
                                  "name" : "01:00 PM",
                                  "value": "780",
                                },
                                {
                                  "name" : "02:00 PM",
                                  "value": "840",
                                },
                                {
                                  "name" : "03:00 PM",
                                  "value": "900",
                                },
                                {
                                  "name" : "04:00 PM",
                                  "value": "960",
                                },
                                {
                                  "name" : "05:00 PM",
                                  "value": "1020",
                                },
                                {
                                  "name" : "06:00 PM",
                                  "value": "1080",
                                },
                                {
                                  "name" : "07:00 PM",
                                  "value": "1140",
                                },
                                {
                                  "name" : "08:00 PM",
                                  "value": "1200",
                                },
                                {
                                  "name" : "09:00 PM",
                                  "value": "1260",
                                },
                                {
                                  "name" : "10:00 PM",
                                  "value": "1320",
                                },
                                {
                                  "name" : "11:00 PM",
                                  "value": "1380",
                                },
                              ]

                            },
                            "type": "select",
                            "id": "cosuSystemUpdatePolicyWindowStartTime"
                          },
                          {
                            "label": "End Time",
                            "tooltip": "Window end time for system update",
                            "optional": {
                              "option": [
                                {
                                  "name" : "12:00 AM",
                                  "value": "1440",
                                },
                                {
                                  "name" : "01:00 AM",
                                  "value": "60",
                                },
                                {
                                  "name" : "02:00 AM",
                                  "value": "120",
                                },
                                {
                                  "name" : "03:00 AM",
                                  "value": "180",
                                },
                                {
                                  "name" : "04:00 AM",
                                  "value": "240",
                                },
                                {
                                  "name" : "05:00 AM",
                                  "value": "300",
                                },
                                {
                                  "name" : "06:00 AM",
                                  "value": "360",
                                },
                                {
                                  "name" : "07:00 AM",
                                  "value": "420",
                                },
                                {
                                  "name" : "08:00 AM",
                                  "value": "480",
                                },
                                {
                                  "name" : "09:00 AM",
                                  "value": "540",
                                },
                                {
                                  "name" : "10:00 AM",
                                  "value": "600",
                                },
                                {
                                  "name" : "11:00 AM",
                                  "value": "660",
                                },
                                {
                                  "name" : "12:00 noon",
                                  "value": "720",
                                },
                                {
                                  "name" : "01:00 PM",
                                  "value": "780",
                                },
                                {
                                  "name" : "02:00 PM",
                                  "value": "840",
                                },
                                {
                                  "name" : "03:00 PM",
                                  "value": "900",
                                },
                                {
                                  "name" : "04:00 PM",
                                  "value": "960",
                                },
                                {
                                  "name" : "05:00 PM",
                                  "value": "1020",
                                },
                                {
                                  "name" : "06:00 PM",
                                  "value": "1080",
                                },
                                {
                                  "name" : "07:00 PM",
                                  "value": "1140",
                                },
                                {
                                  "name" : "08:00 PM",
                                  "value": "1200",
                                },
                                {
                                  "name" : "09:00 PM",
                                  "value": "1260",
                                },
                                {
                                  "name" : "10:00 PM",
                                  "value": "1320",
                                },
                                {
                                  "name" : "11:00 PM",
                                  "value": "1380",
                                },
                              ]

                            },
                            "type": "select",
                            "id": "cosuSystemUpdatePolicyWindowEndTime"
                          },
                        ],
                      },
                    ],
                    "subPanel":[
                      {
                        "key": "Manual",
                        "show": true
                      },                                    ]
                  },
                  "type": "radioGroup",
                  "id": "ENCRYPT_STORAGE"  //toDo change id
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
        {
          "name": "Enrollment Application Install",
          "panels": [{
            "panel":{
              "panelId": "ENROLLMENT_APP_INSTALL",
              "title": "Enrollment Application Install",
              "description": "This configuration can be used to install applications during Android device enrollment.",
              "panelItem": [
                {
                  "label": "This configuration will be applied only during Android device enrollment.",
                  "type": "alert",
                },
                {
                  "label": "Select Enrollment Applications.",
                  "type": "selectTable",
                },
                {
                  "label": "Work profile global user configurations",
                  "type": "title"
                },
                {
                  "label": "App Auto Update Policy",
                  "tooltip": "The Auto-update policy for apps installed on the device",
                  "optional": {
                    "option": [
                      {
                        "name" : "When Connected to WiFi",
                        "value": "0",
                      },
                      {
                        "name" : "Auto Update Anytime",
                        "value": "1",
                      },
                      {
                        "name" : "Ask User to Update",
                        "value": "2",
                      },
                      {
                        "name" : "Disable Auto Update",
                        "value": "3",
                      },
                    ]
                  },
                  "type": "select",
                  "id": "autoUpdatePolicy"
                },
                {
                  "label": "App Availability to a User",
                  "tooltip": "The availability granted to the user for the specified app",
                  "optional": {
                    "option": [
                      {
                        "name" : "All Approved Apps For Enterprise",
                        "value": "1",
                      },
                      {
                        "name" : "All Apps From Playstore",
                        "value": "2",
                      },
                      {
                        "name" : "Only Whitelisted Apps",
                        "value": "3",
                      },
                    ]
                  },
                  "type": "select",
                  "id": "productSetBehavior"
                },
              ],
              "key": "1",
              "show": true
            }
          }]
        },
      ]
    }
  }
};

export default jsonResponse;
