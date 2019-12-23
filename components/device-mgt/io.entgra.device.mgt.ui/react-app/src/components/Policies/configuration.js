const jsonResponse = {
    "PolicyConfigurations": {
        "androidPolicy": {
            "Policy": [
                {
                    "Name": "Passcode Policy",
                    "Panel": [
                        {
                            "panelId": "PASSCODE_POLICY",
                            "PanelItem": [
                                {
                                    "Label": "Allow Simple Value",
                                    "tooltip": "Permits repeating, ascending and descending character sequences",
                                    "Optional": {
                                        "checked": true
                                    },
                                    "_type": "checkbox",
                                    "_id": "allowSimple"
                                },
                                {
                                    "Label": "Require alphanumeric value",
                                    "tooltip": "Mandates to contain both letters and numbers",
                                    "Optional": {
                                        "checked": true
                                    },
                                    "_type": "checkbox",
                                    "_id": "requireAlphanumeric"
                                },
                                {
                                    "Label": "Minimum passcode length",
                                    "tooltip": "Minimum number of characters allowed in a passcode",
                                    "Optional": {
                                        "Option": [
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
                                    "_type": "select",
                                    "_id": "minLength",

                                },
                                {
                                    "Label": "Minimum number of complex characters",
                                    "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                                    "Optional": {
                                        "Option": [
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
                                    "_type": "select",
                                    "_id": "minComplexChars"
                                },
                                {
                                    "Label": "Maximum passcode age in days",
                                    "tooltip": "Number of days after which a passcode must be changed",
                                    "Optional": {
                                        "Placeholder": "Should be in between 1-to-730 days or 0 for none",
                                        "rules":{
                                            "regex": "^(?:0|[1-9]|[1-9][1-9]|[0-6][0-9][0-9]|7[0-2][0-9]|730)$",
                                            "validationMsg": "Should be in between 1-to-730 days or 0 for none",
                                            "required": false
                                        },
                                    },
                                    "_type": "input",
                                    "_id": "maxPINAgeInDays"
                                },
                                {
                                    "Label": "Passcode history",
                                    "tooltip": "Number of consequent unique passcodes to be used before reuse",
                                    "Optional": {
                                        "Placeholder": "Should be in between 1-to-50 passcodes or 0 for none",
                                        "rules":{
                                            "regex": "^(?:0|[1-9]|[1-4][0-9]|50)$",
                                            "validationMsg": "Should be in between 1-to-50 passcodes or 0 for none",
                                            "required": false
                                        },
                                    },
                                    "_type": "input",
                                    "_id": "pinHistory"
                                },
                                {
                                    "Label": "Maximum number of failed attempts",
                                    "tooltip": "The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.",
                                    "Optional": {
                                        "Option": [
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
                                    "_type": "select",
                                    "_id": "maxFailedAttempts"
                                },
                                {
                                    "Label": "Passcode policy for work profile",
                                    "_type": "title"
                                },
                                {
                                    "Label": "Enabled Work profile passcode",
                                    "tooltip": "Enabled Work profile passcode.",
                                    "Optional": {
                                        "checked": false,
                                        "subPanel":
                                            {
                                                "PanelItem": [
                                                    {
                                                        "Label": "Allow Simple Value",
                                                        "tooltip": "Permits repeating, ascending and descending character sequences",
                                                        "Optional": {
                                                            "checked": true
                                                        },
                                                        "_type": "checkbox",
                                                        "_id": "passcodePolicyAllowSimpleWP"
                                                    },
                                                    {
                                                        "Label": "Require alphanumeric value",
                                                        "tooltip": "Mandates to contain both letters and numbers",
                                                        "Optional": {
                                                            "checked": true
                                                        },
                                                        "_type": "checkbox",
                                                        "_id": "passcodePolicyRequireAlphanumericWP"
                                                    },
                                                    {
                                                        "Label": "Minimum passcode length",
                                                        "tooltip": "Minimum number of characters allowed in a passcode",
                                                        "Optional": {
                                                            "Option": [
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
                                                        "_type": "select",
                                                        "_id": "passcodePolicyMinLengthWP"
                                                    },
                                                    {
                                                        "Label": "Minimum number of complex characters",
                                                        "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                                                        "Optional": {

                                                            "Option": [
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
                                                        "_type": "select",
                                                        "_id": "passcodePolicyMinComplexCharsWP"
                                                    },
                                                    {
                                                        "Label": "Maximum passcode age in days",
                                                        "tooltip": "Number of days after which a passcode must be changed",
                                                        "Optional": {
                                                            "Placeholder": "Should be in between 1-to-730 days or 0 for none",
                                                            "rules":{
                                                                "regex": "",
                                                                "validationMsg": "",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "passcodePolicyMaxPasscodeAgeInDaysWP"
                                                    },
                                                    {
                                                        "Label": "Passcode history",
                                                        "tooltip": "Number of consequent unique passcodes to be used before reuse",
                                                        "Optional": {
                                                            "Placeholder": "Should be in between 1-to-50 passcodes or 0 for none",
                                                            "rules":{
                                                                "regex": "",
                                                                "validationMsg": "",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "passcodePolicyPasscodeHistoryWP"
                                                    },
                                                    {
                                                        "Label": "Maximum number of failed attempts",
                                                        "tooltip": "The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.",
                                                        "Optional": {
                                                            "Option": [
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
                                                        "_type": "select",
                                                        "_id": "passcodePolicyMaxFailedAttemptsWP"
                                                    }
                                                ],
                                                "_key": "workProfilePasscode",
                                                "_show": false
                                            }
                                    },
                                    "_type": "checkbox",
                                    "_id": "passcodePolicyWPExist",

                                }
                            ],
                            "_key": "1",
                            "_show": true,
                            "title": "Passcode Policy",
                            "description": "Enforce a configured passcode policy on Android devices. Once this profile is applied," +
                                " the device owners won't be able to modify the password settings on their devices.",
                        },

                    ]
                },
                {
                    "Name": "Restrictions",
                    "Panel": [{
                        "panelId": "CAMERA",
                        "title": "Restrictions",
                        "description": "This configurations can be used to restrict certain settings on an Android device. Once this configuration" +
                            " profile is installed on a device, corresponding users will not be able to modify these settings on their devices.",
                        "PanelItem": [
                            {
                                "Label": "Allow use of camera",
                                "tooltip": "Enables the usage of device camera",
                                "Optional": {
                                    "checked": true
                                },
                                "_type": "checkbox",
                                "_id": "CAMERA"
                            },
                            {
                                "Label": "Below Restrictions are valid only when the Agent is work-profile owner or device owner.",
                                "_type": "alert"
                            },
                            {
                                "Label": "Disallow configuring VPN",
                                "tooltip": "Users are restricted from configuring VPN.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_VPN"
                            },
                            {
                                "Label": "Disallow configuring app control",
                                "tooltip": "Restricts users from modifying applications in the device's settings or launchers.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_APPS_CONTROL"
                            },
                            {
                                "Label": "Disallow cross profile copy paste",
                                "tooltip": "Device owners are restricted from copying items that are copied to the clipboard from the managed profile to the parent profile or vice-versa.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CROSS_PROFILE_COPY_PASTE"
                            },
                            {
                                "Label": "Disallow debugging",
                                "tooltip": "Users are restricted from accessing debug logs.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_DEBUGGING_FEATURES"
                            },
                            {
                                "Label": "Disallow install apps",
                                "tooltip": "Users are restricted from installing applications.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_INSTALL_APPS"
                            },
                            {
                                "Label": "Disallow install from unknown sources",
                                "tooltip": "Users are restricted from installing applications from unknown origin.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_INSTALL_UNKNOWN_SOURCES"
                            },
                            {
                                "Label": "Disallow modify accounts",
                                "tooltip": "Users are restricted from modifying user accounts.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_MODIFY_ACCOUNTS"
                            },
                            {
                                "Label": "Disallow outgoing beam",
                                "tooltip": "Users are restricted from using NFC bump.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_OUTGOING_BEAM"
                            },
                            {
                                "Label": "Disallow location sharing",
                                "tooltip": "Users are restricted from sharing their geo-location.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_SHARE_LOCATION"
                            },
                            {
                                "Label": "Disallow uninstall apps",
                                "tooltip": "Users are restricted from uninstalling applications.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_UNINSTALL_APPS"
                            },
                            {
                                "Label": "Disallow parent profile app linking",
                                "tooltip": "Allows apps in the parent profile to access or handle web links from the managed profile.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "ALLOW_PARENT_PROFILE_APP_LINKING"
                            },
                            {
                                "Label": " Below restrictions will be applicable when the agent is the device owner and Android version 6.0 (Marshmallow) or higher.",
                                "_type": "alert"
                            },
                            {
                                "Label": "Disallow set wallpaper",
                                "tooltip": "Users are restricted from setting wallpapers.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_SET_WALLPAPER"
                            },
                            {
                                "Label": "Disallow set user icon",
                                "tooltip": "Users are restricted from changing their icon.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_SET_USER_ICON"
                            },
                            {
                                "Label": "Disallow remove managed profile",
                                "tooltip": "Users are restricted from removing the managed profile.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_REMOVE_MANAGEMENT_PROFILE"
                            },
                            {
                                "Label": "Disallow autofill",
                                "tooltip": "Users are restricted from using autofill services.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_AUTOFILL"
                            },
                            {
                                "Label": "Disallow bluetooth",
                                "tooltip": "Bluetooth is disallowed on the device.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_BLUETOOTH"
                            },
                            {
                                "Label": "Disallow bluetooth sharing",
                                "tooltip": "Users are restricted from Bluetooth sharing on the device.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_BLUETOOTH_SHARING"
                            },
                            {
                                "Label": "Disallow remove user",
                                "tooltip": "Users are restricted from removing user itself.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_CREDENTIALS"
                            },
                            {
                                "Label": " Below Restrictions are valid only when the Agent is the device owner.",
                                "_type": "alert"
                            },
                            {
                                "Label": "Disallow SMS",
                                "tooltip": "Users are restricted from sending SMS messages.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_SMS"
                            },
                            {
                                "Label": "Ensure verifying apps",
                                "tooltip": "Ensure app verification.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "ENSURE_VERIFY_APPS"
                            },
                            {
                                "Label": "Enable auto timing",
                                "tooltip": "Enables the auto time feature that is in the device's Settings > Data & Time.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "AUTO_TIME"
                            },
                            {
                                "Label": "Disable screen capture",
                                "tooltip": "Screen capturing would be disable.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "SET_SCREEN_CAPTURE_DISABLED"
                            },
                            {
                                "Label": "Disallow volume adjust",
                                "tooltip": "Users are restricted from adjusting device volume.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_ADJUST_VOLUME"
                            },
                            {
                                "Label": "Disallow cell broadcast",
                                "tooltip": "Users are restricted from configuring cell broadcast.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_CELL_BROADCASTS"
                            },
                            {
                                "Label": "Disallow configuring bluetooth",
                                "tooltip": "Users are restricted from configuring bluetooth.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_BLUETOOTH"
                            },
                            {
                                "Label": "Disallow configuring mobile networks",
                                "tooltip": "Users are restricted from configuring mobile network.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_MOBILE_NETWORKS"
                            },
                            {
                                "Label": "Disallow configuring tethering",
                                "tooltip": "Users are restricted from configuring tethering.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_TETHERING"
                            },
                            {
                                "Label": "Disallow configuring WIFI",
                                "tooltip": "Users are restricted from configuring Wifi.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_WIFI"
                            },
                            {
                                "Label": "Disallow safe boot",
                                "tooltip": "Users are restricted to enter safe boot mode.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_SAFE_BOOT"
                            },
                            {
                                "Label": "Disallow outgoing calls",
                                "tooltip": "Users are restricted from taking calls.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_OUTGOING_CALLS"
                            },
                            {
                                "Label": "Disallow mount physical media",
                                "tooltip": "Users are restricted from mounting the device as physical media.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_MOUNT_PHYSICAL_MEDIA"
                            },
                            {
                                "Label": "Disallow create window",
                                "tooltip": "Restricts device owners from opening new windows beside the app windows.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CREATE_WINDOWS"
                            },
                            {
                                "Label": "Disallow factory reset",
                                "tooltip": "Users are restricted from performing factory reset.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_FACTORY_RESET"
                            },
                            {
                                "Label": "Disallow remove user",
                                "tooltip": "Users are restricted from removing user.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_REMOVE_USER"
                            },
                            {
                                "Label": "Disallow add user",
                                "tooltip": "Users are restricted from creating new users.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_ADD_USER"
                            },
                            {
                                "Label": "Disallow network reset",
                                "tooltip": "Users are restricted from resetting network.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_NETWORK_RESET"
                            },
                            {
                                "Label": "Disallow USB file transfer",
                                "tooltip": "Users are restricted from transferring files via USB.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_USB_FILE_TRANSFER"
                            },
                            {
                                "Label": "Disallow unmute microphone",
                                "tooltip": "Users are restricted from unmuting the microphone.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_UNMUTE_MICROPHONE"
                            },
                            {
                                "Label": "Below restrictions will be applied on devices with Android version 6.0 Marshmallow onwards only.",
                                "_type": "alert"
                            },
                            {
                                "Label": "Disable status bar",
                                "tooltip": "Checking this will disable the status bar.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "SET_STATUS_BAR_DISABLED"
                            },
                            {
                                "Label": "Disallow data roaming",
                                "tooltip": "Users are restricted from using cellular data when roaming.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_DATA_ROAMING"
                            },
                            {
                                "Label": "Enable device backup service",
                                "tooltip": "Device backup service wil be enabled.",
                                "Optional": {
                                    "checked": false
                                },
                                "_type": "checkbox",
                                "_id": "DISALLOW_CONFIG_CREDENTIALS"
                            },

                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "Encryption Settings",
                    "Panel": [{
                        "panelId": "ENCRYPT_STORAGE",
                        "title": "Encryption Settings",
                        "description": "This configuration can be used to encrypt data on an Android device, when the device" +
                            " is locked and make it readable when the passcode is entered. Once this configuration profile is installed on a device, " +
                            "corresponding users will not be able to modify these settings on their devices.",
                        "PanelItem": [
                            {
                                "Label": "Un-check following checkbox in case you do not need the device to be encrypted.",
                                "_type": "paragraph",
                            },
                            {
                                "Label": "Enable storage-encryption",
                                "tooltip": "Having this checked would enable Storage-encryption in the device.",
                                "Optional": {
                                    "checked": true
                                },
                                "_type": "checkbox",
                                "_id": "ENCRYPT_STORAGE"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "Wi-Fi Settings",
                    "Panel": [{
                        "panelId": "WIFI",
                        "title": "Wi-Fi Settings",
                        "description": "This configurations can be used to configure Wi-Fi access on an Android device. " +
                            "Once this configuration profile is installed on a device, corresponding users will not be able to modify these settings on their devices.",
                        "PanelItem": [
                            {
                                "Label": "Please note that * sign represents required fields of data.",
                                "_type": "paragraph",
                            },
                            {
                                "Label": "Service Set Identifier (SSID) *",
                                "tooltip": "Identification of the wireless network to be configured.",
                                "Optional": {
                                    "Placeholder": "Should be 1-to-30 characters long",
                                    "rules":{
                                        "regex": "^.{1,30}$",
                                        "validationMsg": "Should be 1-to-30 characters long",
                                        "required": false
                                    },
                                },
                                "_type": "input",
                                "_id": "ssid"
                            },
                            {
                                "Label": "Security *",
                                "tooltip": "Minimum number of complex or non-alphanumeric characters allowed in a passcode",
                                "Optional": {
                                    "Option": [
                                        {
                                            "name" : "None",
                                            "value": "",
                                        },
                                        {
                                            "name" : "WEP",
                                            "value": "wep",
                                        },
                                        {
                                            "name" : "WPA/WPA 2 PSK",
                                            "value": "wpa",
                                        },
                                        {
                                            "name" : "802.1x EAP",
                                            "value": "802eap",
                                        },
                                    ],
                                    "SubPanel":[
                                        {
                                            "PanelItem": [
                                                {
                                                    "Label": "Password *",
                                                    "tooltip": "Password for the wireless network.",
                                                    "Optional": {
                                                        "Placeholder": "",
                                                        "rules":{
                                                            "regex": "",
                                                            "validationMsg": "",
                                                            "required": false
                                                        },
                                                    },
                                                    "_type": "input",
                                                    "_id": "password" //toDo add id
                                                },
                                            ],
                                            "_key": "none",
                                            "_show": true
                                        },
                                        {
                                            "PanelItem": [
                                                {
                                                    "Label": "EAP Method",
                                                    "tooltip": "EAP Method of the wireless network to be configured.",
                                                    "Optional": {
                                                        "Option": [
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
                                                                "value": "sim",
                                                            },
                                                            {
                                                                "name" : "AKA",
                                                                "value": "aka",
                                                            },
                                                        ]
                                                    },
                                                    "_type": "select",
                                                    "_id": "eap"
                                                },
                                                {
                                                    "Label": "Phase 2 Authentication",
                                                    "tooltip": "Phase 2 authentication of the wireless network to be configured.",
                                                    "Optional": {
                                                        "Option": [
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
                                                    "_type": "select",
                                                    "_id": "phase2"
                                                },
                                                {
                                                    "Label": "Identify",
                                                    "tooltip": "Identify of the wireless network to be configured.",
                                                    "Optional": {
                                                        "Placeholder": "Should be 1 to 30 characters long"
                                                    },
                                                    "_type": "input",
                                                    "_id": "identity" //toDo add id
                                                },
                                                {
                                                    "Label": "Anonymous Identity",
                                                    "tooltip": "Identity of the wireless network to be configured.",
                                                    "Optional": {
                                                        "Placeholder": "Should be 1 to 30 characters long"
                                                    },
                                                    "_type": "input",
                                                    "_id": "anonymousIdentity" //toDo add id
                                                },
                                                {
                                                    "Label": "CA Certificate",
                                                    "tooltip": "CA Certificate for the wireless network.",
                                                    "Optional": {
                                                        "Placeholder": ""
                                                    },
                                                    "_type": "upload",
                                                    "_id": "cacert" //toDo add id
                                                },
                                                {
                                                    "Label": "Password *",
                                                    "tooltip": "Password for the wireless network.",
                                                    "Optional": {
                                                        "Placeholder": ""
                                                    },
                                                    "_type": "input",
                                                    "_id": "XXXXXXXXXXXX" //toDo add id
                                                },
                                            ],
                                            "_key": "EAP",
                                            "_show": true
                                        },
                                    ]
                                },
                                "_type": "select",
                                "_id": "type"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    },
                    ]
                },
                {
                    "Name": "Global Proxy Settings",
                    "Panel": [
                        {
                            "panelId": "GLOBAL_PROXY",
                            "title": "Global Proxy Settings",
                            "description": "This configurations can be used to set a network-independent global HTTP proxy on an " +
                                "Android device. Once this configuration profile is installed on a device, all the network traffic will " +
                                "be routed through the proxy server.",
                            "PanelItem": [
                                {
                                    "Label": "This profile requires the agent application to be the device owner.",
                                    "_type": "alert"
                                },
                                {
                                    "Label": "This proxy is only a recommendation and it is possible that some apps will ignore it.",
                                    "_type": "alert"
                                },
                                {
                                    "Label": "Please note that * sign represents required fields of data.",
                                    "_type": "paragraph",
                                },
                                {
                                    "Label": "Proxy Configuration Type",
                                    "tooltip": "Select the configuration type.",
                                    "Optional": {
                                        "Radio": [
                                            {
                                                "name" : "Manual",
                                                "value": "MANUAL",
                                                "subPanel": [
                                                    {
                                                        "Label": "Proxy Host *",
                                                        "tooltip": "Host name/IP address of the proxy server.",
                                                        "Optional": {
                                                            "Placeholder": "192.168.8.1",
                                                            "rules":{
                                                                "regex": "^(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})\\.(0|[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]{1,3})$",
                                                                "validationMsg": "Please enter valid IP address",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "proxyHost" //toDo add id
                                                    },
                                                    {
                                                        "Label": "Proxy Port *",
                                                        "tooltip": "Target port for the proxy server.",
                                                        "Optional": {
                                                            "Placeholder": "Target port 0 - 65535",
                                                            "rules":{
                                                                "regex": "^(?:0|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5])$",
                                                                "validationMsg": "Please enter valid port",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "proxyPort" //toDo add id
                                                    },
                                                    {
                                                        "Label": "Proxy Exclusion List",
                                                        "tooltip": "Add host names to this separated by commas to prevent them" +
                                                            "from routing through the proxy server. The hostname entries can be wildcards such as" +
                                                            "*.example.com",
                                                        "Optional": {
                                                            "Placeholder": "Example : localhost, *.example.com",
                                                            "rules":{
                                                                "regex": "",
                                                                "validationMsg": "",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "proxyExclList" //toDo add id
                                                    },
                                                ],
                                            },
                                            {
                                                "name" : "Auto",
                                                "value": "AUTO",
                                                "subPanel": [
                                                    {
                                                        "Label": "Proxy PAC File URL *",
                                                        "tooltip": "URL for the proxy auto config PAC script",
                                                        "Optional": {
                                                            "Placeholder": "http://exampleproxy.com/proxy.pac",
                                                            "rules":{
                                                                "regex": "",
                                                                "validationMsg": "",
                                                                "required": false
                                                            },
                                                        },
                                                        "_type": "input",
                                                        "_id": "proxyPacUrl" //toDo add id
                                                    },
                                                ],
                                            },
                                        ],
                                    },
                                    "_type": "radioGroup",
                                    "_id": "proxyConfigType"  //toDo change id
                                },
                            ],
                            "_key": "1",
                            "_show": true
                        },
                    ]
                },
                {
                    "Name": "Virtual Private Network",
                    "Panel": [
                        {
                            "panelId": "VPN",
                            "title": "VPN Settings",
                            "description": "Configure the OpenVPN settings on Android devices. In order to enable this, device needs to have \"OpenVPN for Android\" application installed.",
                            "PanelItem": [
                                {
                                    "Label": "OpenVPN Server Config file",
                                    "tooltip": "OpenVPN configurations ovpn file.",
                                    "Optional": {
                                        "Placeholder": ""
                                    },
                                    "_type": "upload",
                                    "_id": "XXXXXXXXXXvccvXX" //toDo add id
                                },

                            ],
                            "_key": "cxcx1",
                            "_show": true
                        },
                        {
                            "panelId": "VPN_ON",
                            "title": "Always On VPN Settings",
                            "description": "Configure an always-on VPN connection through a specific VPN client application.",
                            "PanelItem": [
                                {
                                    "Label": "Below configurations are valid only when the Agent is work-profile owner or device owner.",
                                    "_type": "alert"
                                },
                                {
                                    "Label": "VPN Client Application Package Name* ",
                                    "tooltip": "Package name of the VPN client application to be configured.",
                                    "Optional": {
                                        "Placeholder": "Should be a valid package name",
                                        "rules":{
                                            "regex": "",
                                            "validationMsg": "",
                                            "required": false
                                        },
                                    },
                                    "_type": "input",
                                    "_id": "packageName" //toDo add id
                                },
                            ],
                            "_key": "1zxz",
                            "_show": true
                        }
                    ]
                },
                {
                    "Name": "Certificates Install",
                    "Panel": [{
                        "panelId": "INSTALL_CERT",
                        "title": "Certificate Install Settings",
                        "description": "Configure the certificate install settings on Android devices.",
                        "PanelItem": [
                            {
                                "Label": "Added Certificate List",
                                "tooltip": "Add a certificate.",
                                "Optional": {
                                    "button": {
                                        "id": "addCertificate",
                                        "name": " Add Certificate"
                                    },
                                    "dataSource": "cert-list",
                                    "columns": [
                                        {
                                            "name" : "Certificate Name",
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
                                "_type": "inputTable",
                                "_id": "CERT_LIST"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "Work-Profile Configurations",
                    "Panel": [{
                        "panelId": "WORK_PROFILE",
                        "title": "Work-Profile Configurations",
                        "description": "Configure these settings to manage the applications in the work profile.",
                        "PanelItem": [
                            {
                                "Label": "Profile Name",
                                "tooltip": "Name of the Work-Profile created by IOT Server Agent.",
                                "Optional": {
                                    "Placeholder": "",
                                    "rules":{
                                        "regex": "",
                                        "validationMsg": "",
                                        "required": false
                                    },
                                },
                                "_type": "input",
                                "_id": "XXXXXXXXczxczxXXXX" //toDo add id
                            },
                            {
                                "Label": "Enable System Apps",
                                "tooltip": "The set of system apps needed to be added to the work-profile.",
                                "Optional": {
                                    "Placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                                    "Row": 4
                                },
                                "_type": "textArea",
                                "_id": "ENCRYPT_STxcxORAGcxcxcE"
                            },
                            {
                                "Label": "Hide System Apps",
                                "tooltip": "The set of system apps needed to be hide in the work-profile.",
                                "Optional": {
                                    "Placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                                    "Row": 4
                                },
                                "_type": "textArea",
                                "_id": "ENCRYPccxT_STORAGcxcxcE"
                            },
                            {
                                "Label": "Unhide System Apps",
                                "tooltip": "The set of system apps needed to be unhide in the work-profile.",
                                "Optional": {
                                    "Placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                                    "Row": 4
                                },
                                "_type": "textArea",
                                "_id": "ENCRYPccxT_STORAGcxcxcE"
                            },
                            {
                                "Label": "Enable Google PlayStore Apps",
                                "tooltip": "The set of apps needed to be installed from Google PlayStore to work-profile.",
                                "Optional": {
                                    "Placeholder": "Should be exact package names separated by commas. Ex: com.google.android.apps.maps, com.google.android.calculator",
                                    "Row": 4
                                },
                                "_type": "textArea",
                                "_id": "ENCRYPccxT_STORAGcxcxcE"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "COSU Profile Configurations",
                    "Panel": [{
                        "panelId": "COSU_PROFILE",
                        "title": "COSU Profile Configurations",
                        "description": "This policy can be used to configure the profile of COSU Devices.",
                        "PanelItem": [
                            {
                                "Label": "Restrict Device Operation Time",
                                "tooltip": "",
                                "Optional": {
                                    "checked": false,
                                    "subPanel":
                                        {
                                            "PanelItem": [
                                                {
                                                    "Label": "Start Time",
                                                    "tooltip": "Start time for the device",
                                                    "Optional": {
                                                        "Option": [
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
                                                    "_type": "select",
                                                    "_id": "passcodePolicyMinLengthWP"
                                                },
                                                {
                                                    "Label": "End Time",
                                                    "tooltip": "Lock-down time for the device",
                                                    "Optional": {
                                                        "Option": [
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
                                                    "_type": "select",
                                                    "_id": "ENDTIME"
                                                },
                                                {
                                                    "Label": "Device will be operable only during the above period.",
                                                    "_type": "alert"
                                                },
                                            ],
                                            "_key": "RestrictDeviceOperationTime",
                                            "_show": true
                                        }
                                },
                                "_type": "checkbox",
                                "_id": "ENCRYPT_STOcxcxcRAGhftyE"
                            },
                            {
                                "Label": "Device Global Configuration",
                                "tooltip": "",
                                "Optional": {
                                    "checked": false,
                                    "subPanel": {
                                        "PanelItem": [
                                            {
                                                "Label": "Launcher background image",
                                                "tooltip": "This is the image that will be displayed in kiosk background.",
                                                "Optional": {
                                                    "Placeholder": "Should be a valid URL of jpg or jpeg or png",
                                                    "rules":{
                                                        "regex": "",
                                                        "validationMsg": "",
                                                        "required": false
                                                    },
                                                },
                                                "_type": "input",
                                                "_id": "XXXXXXXsdsdXXXXX" //toDo add id
                                            },
                                            {
                                                "Label": "Company logo to display",
                                                "tooltip": "Company logo to display",
                                                "Optional": {
                                                    "Placeholder": "Should be a valid URL ending with .jpg, .png, .jpeg",
                                                    "rules":{
                                                        "regex": "",
                                                        "validationMsg": "",
                                                        "required": false
                                                    },
                                                },
                                                "_type": "input",
                                                "_id": "XXXXXXXsdsdXXXXX" //toDo add id
                                            },
                                            {
                                                "Label": "Company name",
                                                "tooltip": "Company name",
                                                "Optional": {
                                                    "Placeholder": "Name to appear on the agent",
                                                    "rules":{
                                                        "regex": "",
                                                        "validationMsg": "",
                                                        "required": false
                                                    },
                                                },
                                                "_type": "input",
                                                "_id": "XXXXXXXsdsdXXXXX" //toDo add id
                                            },
                                            {
                                                "Label": "Is single application mode",
                                                "tooltip": "Is single application mode",
                                                "Optional": {
                                                    "checked": false,
                                                    "subPanel": {
                                                            "PanelItem": [
                                                                {
                                                                    "Label": "Selected initial app in Enrollment Application Install" +
                                                                        " policy config will be selected for single application mode.",
                                                                    "_type": "alert",
                                                                },
                                                                {
                                                                    "Label": "Is application built for Kiosk",
                                                                    "tooltip": "Is single mode app built for Kisosk. Enable if lock task method is called in the " +
                                                                        "application",
                                                                    "Optional": {
                                                                        "checked": true,
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "Efdgdg"
                                                                },
                                                            ],
                                                            "_key": "SingleAppMode",
                                                            "_show": true
                                                        },

                                                },
                                                "_type": "checkbox",
                                                "_id": "ENCRsdfsdfdAGE"
                                            },
                                            {
                                                "Label": "Is idle media enabled ",
                                                "tooltip": "Is idle media enabled ",
                                                "Optional": {
                                                    "checked": false,
                                                    "subPanel": {
                                                            "PanelItem": [
                                                                {
                                                                    "Label": "Media to display while idle",
                                                                    "tooltip": "Media to display while the device is idle",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a valid URL ending with .jpg, .png, .jpeg, .mp4, .3gp, .wmv, .mkv",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "XXXXXXXsdsdXXXXX" //toDo add id
                                                                },
                                                                {
                                                                    "Label": "Idle graphic begin after(seconds)",
                                                                    "tooltip": "Idle graphic begin after the defined seconds",
                                                                    "Optional": {
                                                                        "Placeholder": "Idle timeout in seconds",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "XXXXXXXsdsdXXXXX" //toDo add id
                                                                },
                                                            ],
                                                            "_key": "idleMediaEnabled",
                                                            "_show": true
                                                        }
                                                },
                                                "_type": "checkbox",
                                                "_id": "ENCsdsdsORAGE"
                                            },
                                            {
                                                "Label": "Is multi-user device",
                                                "tooltip": "Is multi-user device.",
                                                "Optional": {
                                                    "checked": false,
                                                    "subPanel":{
                                                            "PanelItem": [
                                                                {
                                                                    "Label": "Is login needed for user switch",
                                                                    "tooltip": "Permits repeating, ascending and descending character sequences",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isLoginRequired"
                                                                },
                                                                {
                                                                    "Label": " Provide comma separated package name or web clip details for applications.\n" +
                                                                        "eg: com.google.android.apps.maps, {\"identity\":\"http:entgra.io/\",\"title\":\"entgra-webclip\"}",
                                                                    "_type": "alert",
                                                                },
                                                                {
                                                                    "Label": "Primary User Apps ",
                                                                    "tooltip": "Primary User is the user to which the device is enrolled",
                                                                    "Optional": {
                                                                        "Placeholder": "Applications",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "primaryUserApps" //toDo add id
                                                                },
                                                                {
                                                                    "Label": "Add User Applications",
                                                                    "tooltip": "Add User Applications.",
                                                                    "Optional": {
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
                                                                    "_type": "inputTable",
                                                                    "_id": "ENCRYcbchdPT_STORAGE"
                                                                },
                                                            ],
                                                            "_key": "idleMediaEnabled",
                                                            "_show": true
                                                        }
                                                },
                                                "_type": "checkbox",
                                                "_id": "isMultiUserDevice"
                                            },
                                            {
                                                "Label": "Device display orientation",
                                                "tooltip": "Device display orientation",
                                                "Optional": {
                                                    "Option": [
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
                                                "_type": "select",
                                                "_id": "minCompzzdlexChars"
                                            },
                                            {
                                                "Label": "Enable Browser Properties",
                                                "tooltip": "Browser Properties",
                                                "Optional": {
                                                    "checked": false,
                                                    "subPanel": {
                                                            "PanelItem": [
                                                                {
                                                                    "Label": "Primary URL ",
                                                                    "tooltip": "Primary URL",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a valid URL",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "primaryURL" //toDo add id
                                                                },
                                                                {
                                                                    "Label": "Enable browser address bar",
                                                                    "tooltip": "Enables address bar of the browser",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isAddressBarEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is allow to go back on a page",
                                                                    "tooltip": "Allow to go back in a page",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "showBackController"
                                                                },
                                                                {
                                                                    "Label": "Is it allowed to go forward in browser",
                                                                    "tooltip": "Is it allowed to go forward in a web page",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isForwardControllerEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is home button enabled",
                                                                    "tooltip": "Is home button enabled",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isHomeButtonEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is page reload enabled",
                                                                    "tooltip": "Is page reload enabled",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isReloadEnabled"
                                                                },
                                                                {
                                                                    "Label": "Only allowed to visit the primary url",
                                                                    "tooltip": "Only allowed to visit the primary url",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "lockToPrimaryURL"
                                                                },
                                                                {
                                                                    "Label": "Is javascript enabled",
                                                                    "tooltip": "Is javascript enabled",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isJavascriptEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is copying to visit the primary url",
                                                                    "tooltip": "Is copying to visit the primary url",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isTextCopyEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is downloading files enabled",
                                                                    "tooltip": "Is downloading files enabled",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isDownloadsEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is Kiosk limited to one webapp",
                                                                    "tooltip": "Is Kiosk limited to one webapp.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isLockedToBrowser"
                                                                },
                                                                {
                                                                    "Label": "Is form auto-fill enabled",
                                                                    "tooltip": "Is form auto-fill enabled.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isFormAutoFillEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is content access enabled",
                                                                    "tooltip": "Enables or disable content URL access within WebView. Content URL" +
                                                                        "access allows WebView to load content from a content provider installed in the system.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isContentAccessEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is file access allowed",
                                                                    "tooltip": "Sets whether javascript running in the context of a file schema URL should be" +
                                                                        "allowed to access content from other file scheme URLs.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isFileAccessAllowed"
                                                                },
                                                                {
                                                                    "Label": "Is allowed universal access from file URLs",
                                                                    "tooltip": "Sets whether JavaScript running in the context of a file scheme URL should be allowed" +
                                                                        "to access content from any origin",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isAllowedUniversalAccessFromFileURLs"
                                                                },
                                                                {
                                                                    "Label": "Is application cache enabled",
                                                                    "tooltip": "Is application cache enabled",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isAppCacheEnabled"
                                                                },
                                                                {
                                                                    "Label": "Application cache file path",
                                                                    "tooltip": "Sets the path to the Application Cache files. In order for the Application Caches API" +
                                                                        "to be enabled, this method must be called with a path to which the application can write",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a valid path",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "appCachePath"
                                                                },
                                                                {
                                                                    "Label": "Application cache mode",
                                                                    "tooltip": "Overrides the way the cache is used. The way the cache is used is based on the navigation" +
                                                                        "type. For a normal page load, the cache is checked and content is re-validated as needed. " +
                                                                        "When navigating back, content is not revalidated, instead the content is just retrieved from the cache." +
                                                                        "This method allows the client to override this behavior by specifying one of LOAD_DEFAULT," +
                                                                        "LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE or LOAD_CACHE_ONLY",
                                                                    "Optional": {
                                                                        "Option": [
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
                                                                    "_type": "select",
                                                                    "_id": "cacheMode"
                                                                },
                                                                {
                                                                    "Label": "Should load images",
                                                                    "tooltip": "Sets whether the browser should load image resources (through network and cached)." +
                                                                        "Note that this method controls loading of all images, including those embedded using the data URI" +
                                                                        "scheme.",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isLoadsImagesAutomatically"
                                                                },
                                                                {
                                                                    "Label": "Block image loads via network",
                                                                    "tooltip": "Sets whether the browser should not load image resources from the network" +
                                                                        "(resources accessed via http and https URI schemes)",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isBlockNetworkImage"
                                                                },
                                                                {
                                                                    "Label": "Block all resource loads from network",
                                                                    "tooltip": "Sets whether the browser should not load any resources from the network.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isBlockNetworkLoads"
                                                                },
                                                                {
                                                                    "Label": "Support zooming",
                                                                    "tooltip": "Sets whether the browser should support zooming using its on-screen zoom" +
                                                                        "controls and gestures",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isSupportZoomEnabled"
                                                                },
                                                                {
                                                                    "Label": "Show on-screen zoom controllers",
                                                                    "tooltip": "Sets whether the browser should display on-screen zoom controls. Gesture based controllers" +
                                                                        "are still available",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isDisplayZoomControls"
                                                                },
                                                                {
                                                                    "Label": "Text zoom percentage",
                                                                    "tooltip": "Sets the text zoom of the page in percent",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a positive number",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "textZoom"
                                                                },
                                                                {
                                                                    "Label": "Default font size",
                                                                    "tooltip": "Sets the default font size",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a positive number between 1 and 72",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "defaultFontSize"
                                                                },
                                                                {
                                                                    "Label": "Default text encoding name",
                                                                    "tooltip": "Sets the default text encoding name to use when decoding html pages",
                                                                    "Optional": {
                                                                        "Placeholder": "Should a valid text encoding",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "defaultTextEncodingName"
                                                                },
                                                                {
                                                                    "Label": "Is database storage API enabled",
                                                                    "tooltip": "Sets whether the database storage API is enabled.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isDatabaseEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is DOM storage API enabled",
                                                                    "tooltip": "Sets whether the DOM storage API is enabled.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isDomStorageEnabled"
                                                                },
                                                                {
                                                                    "Label": "Is Geo-location enabled",
                                                                    "tooltip": "Sets whether Geo-location API is enabled.",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "geolocationEnabled"
                                                                },
                                                                {
                                                                    "Label": "Can JavaScript open windows",
                                                                    "tooltip": "JavaScript can open window automatically or not. This applies to the JavaScript" +
                                                                        "function window.open()",
                                                                    "Optional": {
                                                                        "checked": false
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isJavaScriptCanOpenWindowsAutomatically"
                                                                },
                                                                {
                                                                    "Label": "Does media playback requires user consent",
                                                                    "tooltip": "Sets whether the browser requires a user gesture to play media. If false, the browser" +
                                                                        "can play media without user consent",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isMediaPlaybackRequiresUserGesture"
                                                                },
                                                                {
                                                                    "Label": "Is safe browsing enabled",
                                                                    "tooltip": "Sets whether safe browsing in enabled. Safe browsing allows browser to protect against malware and" +
                                                                        " phishing attacks by verifying the links.",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isSafeBrowsingEnabled"
                                                                },
                                                                {
                                                                    "Label": "Use wide view port",
                                                                    "tooltip": "Sets whether the browser should enable support for the viewport HTML meta tag or should" +
                                                                        "use a wide viewport. When the value of the setting is false, the layout width is always set to the " +
                                                                        "width of the browser control in  device-independent (CSS) pixels. When the value is true and the" +
                                                                        "page contains the viewport meta tag, the value of the width specified in th tag is used. If the page" +
                                                                        "does not contain the tag or does not provide a width, then a wide viewport will be used",
                                                                    "Optional": {
                                                                        "checked": true
                                                                    },
                                                                    "_type": "checkbox",
                                                                    "_id": "isUseWideViewPort"
                                                                },
                                                                {
                                                                    "Label": "Browser user agent string",
                                                                    "tooltip": "Sets the WebView's user-agent string",
                                                                    "Optional": {
                                                                        "Placeholder": "Should be a valid user agent string",
                                                                        "rules":{
                                                                            "regex": "",
                                                                            "validationMsg": "",
                                                                            "required": false
                                                                        },
                                                                    },
                                                                    "_type": "input",
                                                                    "_id": "userAgentString" //toDo add id
                                                                },
                                                                {
                                                                    "Label": "Mixed content mode",
                                                                    "tooltip": "Configures the browser's behavior when a secure origin attempts to load a resource" +
                                                                        "from an insecure origin",
                                                                    "Optional": {
                                                                        "Option": [
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
                                                                    "_type": "select",
                                                                    "_id": "mixedContentMode"
                                                                },

                                                            ],
                                                            "_key": "idleMediaEnabled",
                                                            "_show": true
                                                        }
                                                },
                                                "_type": "checkbox",
                                                "_id": "ENCsdsdcxzcxsORAGE"
                                            },
                                            {
                                                "Label": "Global configurations related to device.",
                                                "_type": "alert",
                                            },
                                        ],
                                        "_key": "DeviceGlobalConfiguration",
                                        "_show": true
                                    },
                                },
                                "_type": "checkbox",
                                "_id": "ENCRYPT_STOxcxcRAGE"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    },



                    ]
                },
                {
                    "Name": "Application Restrictions",
                    "Panel": [{
                        "panelId": "APP-RESTRICTION",
                        "title": "Application Restriction Setting",
                        "description": "This configuration can be used to encrypt data on an Android device, when the device is locked and make it " +
                            "readable when the passcode is entered. Once this configuration profile is installed on a device, corresponding users" +
                            " will not be able to modify these settings on their devices.",
                        "PanelItem": [
                            {
                                "Label": "Select type",
                                "tooltip": "Select a type to proceed",
                                "Optional": {
                                    "Option": [
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
                                "_type": "select",
                                "_id": "restrictionType"
                            },
                            {
                                "Label": "Restricted Application List",
                                "tooltip": "Add an application to restrict.",
                                "Optional": {
                                    "button": {
                                        "id": "addApplication",
                                        "name": " Add Application"
                                    },
                                    "dataSource": "restricted-applications",
                                    "columns": [
                                        {
                                            "name" : "Application Name/Description",
                                            "key" : "appName",
                                            "type": "input",
                                            "others": {
                                                "placeholder":"Gmail",
                                                "inputType": "email"
                                            }
                                        },
                                        {
                                            "name" : "Package Name",
                                            "key" : "packageName",
                                            "type": "input",
                                            "others": {
                                                "placeholder":"com.google.android.gm",
                                                "inputType": "text"
                                            }
                                        },
                                    ]
                                },
                                "_type": "inputTable",
                                "_id": "RestrictedApplicationList"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "Runtime Permission Policy (COSU)",
                    "Panel": [{
                        "panelId": "RUNTIME_PERMISSION_POLICY",
                        "title": "Runtime Permission Policy (COSU / Work Profile)",
                        "description": "This configuration can be used to set a runtime permission policy to an Android Device.",
                        "PanelItem": [
                            {
                                "Label": "Set default runtime permission",
                                "tooltip": "When an app requests a runtime permission this enforces whether the user needs" +
                                    " to prompted or the permission either automatically granted or denied",
                                "Optional": {
                                    "Option": [
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
                                "_type": "select",
                                "_id": "defaultPermissionType"
                            },
                            {
                                "Label": "Set app-specific runtime permissions",
                                "tooltip": "Add an application and set permission policy for a specific permission it need.",
                                "Optional": {
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
                                            "name" : "Package Name",
                                            "key" : "packageName",
                                            "type": "input",
                                            "others": {
                                                "placeholder":"com.google.android.pay",
                                                "inputType": "text"
                                            }
                                        },
                                        {
                                            "name" : "Permission Name",
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
                                "_type": "inputTable",
                                "_id": "restricted-applications"
                            },
                            {
                                "Label": "Already granted or denied permissions are not affected by this policy.",
                                "_type": "alert",
                            },
                            {
                                "Label": "Permissions can be granted or revoked only for applications built with a Target SDK Version of Android Marshmallow or later.",
                                "_type": "alert",
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "System Update Policy (COSU)",
                    "Panel": [{
                        "panelId": "SYSTEM_UPDATE_POLICY",
                        "title": "System Update Policy (COSU)",
                        "description": "This configuration can be used to set a passcode policy to an Android Device. Once this" +
                            " configuration profile is installed on a device, corresponding users will not be able to modify " +
                            "these settings on their devices.",
                        "PanelItem": [
                            {
                                "Label": "System Update",
                                "tooltip": "Type of the System Update to be set by the Device Owner.",
                                "Optional": {
                                    "Radio": [
                                        {
                                            "name": "Automatic",
                                            "value": "automatic",
                                            "subPanel": [{
                                                "_type": "none",
                                            }]
                                        },
                                        {
                                            "name": "Postpone",
                                            "value": "postpone",
                                            "subPanel": [{
                                                "_type": "none",
                                            }]
                                        },
                                        {
                                            "name": "Window",
                                            "value": "window",
                                            "subPanel": [
                                                {
                                                    "Label": "Below configuration of start time and end time are valid only when window option is selected.",
                                                    "_type": "alert",
                                                },
                                                {
                                                    "Label": "Start Time",
                                                    "tooltip": "Window start time for system update",
                                                    "Optional": {
                                                        "Option": [
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
                                                    "_type": "select",
                                                    "_id": "cosuSystemUpdatePolicyWindowStartTime"
                                                },
                                                {
                                                    "Label": "End Time",
                                                    "tooltip": "Window end time for system update",
                                                    "Optional": {
                                                        "Option": [
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
                                                    "_type": "select",
                                                    "_id": "cosuSystemUpdatePolicyWindowEndTime"
                                                },
                                            ],
                                        },
                                    ],
                                    "SubPanel":[
                                        {

                                            "_key": "Manual",
                                            "_show": true
                                        },                                    ]


                                },
                                "_type": "radioGroup",
                                "_id": "ENCRYPT_STORAGE"  //toDo change id
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
                {
                    "Name": "Enrollment Application Install",
                    "Panel": [{
                        "panelId": "ENROLLMENT_APP_INSTALL",
                        "title": "Enrollment Application Install",
                        "description": "This configuration can be used to install applications during Android device enrollment.",
                        "PanelItem": [
                            {
                                "Label": "This configuration will be applied only during Android device enrollment.",
                                "_type": "alert",
                            },
                            {
                                "Label": "Select Enrollment Applications.",
                                "_type": "selectTable",
                            },
                            {
                                "Label": "Work profile global user configurations",
                                "_type": "title"
                            },
                            {
                                "Label": "App Auto Update Policy",
                                "tooltip": "The Auto-update policy for apps installed on the device",
                                "Optional": {
                                    "Option": [
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
                                "_type": "select",
                                "_id": "autoUpdatePolicy"
                            },
                            {
                                "Label": "App Availability to a User",
                                "tooltip": "The availability granted to the user for the specified app",
                                "Optional": {
                                    "Option": [
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
                                "_type": "select",
                                "_id": "productSetBehavior"
                            },
                        ],
                        "_key": "1",
                        "_show": true
                    }]
                },
            ]
        }
    }
};

export default jsonResponse;