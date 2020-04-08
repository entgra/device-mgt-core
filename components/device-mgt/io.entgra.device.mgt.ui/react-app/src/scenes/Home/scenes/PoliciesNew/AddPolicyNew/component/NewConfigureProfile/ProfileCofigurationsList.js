const policyUIConfigurations = {
  policies: [
    {
      description:
        "Enforce a configured passcode policy on Android devices. Once this profile is applied, the device owners won't be able to modify the password settings on their devices.",
      name: 'Passcode Policy',
      features: [
        {
          featureCode: 'PASSCODE_POLICY',
          contents: [
            {
              key: 'content',
              items: [
                {
                  label: 'Allow Simple Value',
                  tooltip:
                    'Permits repeating, ascending and descending character sequences',
                  key: 'allowSimple',
                  value: true,
                  isRequired: true,
                  checkbox: {},
                },
                {
                  label: 'Require alphanumeric value',
                  tooltip: 'Mandates to contain both letters and numbers',
                  key: 'requireAlphanumeric',
                  value: true,
                  isRequired: true,
                  checkbox: {},
                },
                {
                  label: 'Minimum passcode length',
                  tooltip: 'Minimum number of characters allowed in a passcode',
                  key: 'minLength',
                  value: 'None',
                  isRequired: true,
                  select: {
                    valueType: 'Integer',
                    options: [
                      {
                        definedValue: 'None',
                        displayingValue: 'None',
                      },
                      {
                        definedValue: '4',
                        displayingValue: '04',
                      },
                      {
                        definedValue: '5',
                        displayingValue: '05',
                      },
                      {
                        definedValue: '6',
                        displayingValue: '06',
                      },
                      {
                        definedValue: '7',
                        displayingValue: '07',
                      },
                      {
                        definedValue: '8',
                        displayingValue: '08',
                      },
                      {
                        definedValue: '9',
                        displayingValue: '09',
                      },
                      {
                        definedValue: '10',
                        displayingValue: '10',
                      },
                      {
                        definedValue: '11',
                        displayingValue: '11',
                      },
                      {
                        definedValue: '12',
                        displayingValue: '12',
                      },
                      {
                        definedValue: '13',
                        displayingValue: '13',
                      },
                      {
                        definedValue: '14',
                        displayingValue: '14',
                      },
                      {
                        definedValue: '15',
                        displayingValue: '15',
                      },
                    ],
                  },
                },
                {
                  label: 'Minimum number of complex characters',
                  tooltip:
                    'Minimum number of complex or non-alphanumeric characters allowed in a passcode',
                  key: 'minComplexChars',
                  value: 'None',
                  isRequired: true,
                  select: {
                    valueType: 'Integer',
                    options: [
                      {
                        definedValue: 'None',
                        displayingValue: 'None',
                      },
                      {
                        definedValue: '1',
                        displayingValue: '01',
                      },
                      {
                        definedValue: '2',
                        displayingValue: '02',
                      },
                      {
                        definedValue: '3',
                        displayingValue: '03',
                      },
                      {
                        definedValue: '4',
                        displayingValue: '04',
                      },
                      {
                        definedValue: '5',
                        displayingValue: '05',
                      },
                    ],
                  },
                },
                {
                  label: 'Maximum passcode age in days',
                  tooltip:
                    'Number of days after which a passcode must be changed',
                  key: 'maxPINAgeInDays',
                  value: null,
                  isRequired: false,
                  input: {
                    type: 'TextArea',
                    placeholderValue:
                      'Should be in between 1-to-730 days or 0 for none',
                    regEx:
                      '^(?:0|[1-9]|[1-9][1-9]|[0-6][0-9][0-9]|7[0-2][0-9]|730)$',
                    validationMessage:
                      'Should be in between 1-to-730 days or 0 for none',
                  },
                },
                {
                  label: 'Passcode history',
                  tooltip:
                    'Number of consequent unique passcodes to be used before reuse',
                  key: 'pinHistory',
                  value: null,
                  isRequired: false,
                  input: {
                    type: 'TextArea',
                    placeholderValue:
                      'Should be in between 1-to-50 passcodes or 0 for none',
                    regEx: '^(?:0|[1-9]|[1-4][0-9]|50)$',
                    validationMessage:
                      'Should be in between 1-to-50 passcodes or 0 for none',
                  },
                },
                {
                  label: 'Maximum number of failed attempts',
                  tooltip:
                    'The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.',
                  key: 'maxFailedAttempts',
                  value: 'None',
                  isRequired: true,
                  select: {
                    valueType: 'Integer',
                    options: [
                      {
                        definedValue: 'None',
                        displayingValue: 'None',
                      },
                      {
                        definedValue: '3',
                        displayingValue: '03',
                      },
                      {
                        definedValue: '4',
                        displayingValue: '04',
                      },
                      {
                        definedValue: '5',
                        displayingValue: '05',
                      },
                      {
                        definedValue: '6',
                        displayingValue: '06',
                      },
                      {
                        definedValue: '7',
                        displayingValue: '07',
                      },
                      {
                        definedValue: '8',
                        displayingValue: '08',
                      },
                      {
                        definedValue: '9',
                        displayingValue: '09',
                      },
                      {
                        definedValue: '10',
                        displayingValue: '10',
                      },
                    ],
                  },
                },
                {
                  label: 'Enabled Work profile passcode',
                  tooltip: 'Enabled Work profile passcode.',
                  key: 'passcodePolicyWPExist',
                  value: false,
                  isRequired: false,
                  subTitle: 'Passcode policy for work profile',
                  checkbox: {},
                },
              ],
              subContents: [
                {
                  key: 'workProfilePasscode',
                  items: [
                    {
                      label: 'Allow Simple Value',
                      tooltip:
                        'Permits repeating, ascending and descending character sequences',
                      key: 'allowSimpleWP',
                      value: true,
                      isRequired: false,
                      checkbox: {},
                    },
                    {
                      label: 'Require alphanumeric value',
                      tooltip: 'Mandates to contain both letters and numbers',
                      key: 'requireAlphanumericWP',
                      value: true,
                      isRequired: false,
                      checkbox: {},
                    },
                    {
                      label: 'Minimum passcode length',
                      tooltip:
                        'Minimum number of characters allowed in a passcode',
                      key: 'minLengthWP',
                      value: 'None',
                      isRequired: false,
                      select: {
                        valueType: 'Integer',
                        options: [
                          {
                            definedValue: 'None',
                            displayingValue: 'None',
                          },
                          {
                            definedValue: '4',
                            displayingValue: '04',
                          },
                          {
                            definedValue: '5',
                            displayingValue: '05',
                          },
                          {
                            definedValue: '6',
                            displayingValue: '06',
                          },
                          {
                            definedValue: '7',
                            displayingValue: '07',
                          },
                          {
                            definedValue: '8',
                            displayingValue: '08',
                          },
                          {
                            definedValue: '9',
                            displayingValue: '09',
                          },
                          {
                            definedValue: '10',
                            displayingValue: '10',
                          },
                          {
                            definedValue: '11',
                            displayingValue: '11',
                          },
                          {
                            definedValue: '12',
                            displayingValue: '12',
                          },
                          {
                            definedValue: '13',
                            displayingValue: '13',
                          },
                          {
                            definedValue: '14',
                            displayingValue: '14',
                          },
                          {
                            definedValue: '15',
                            displayingValue: '15',
                          },
                        ],
                      },
                    },
                    {
                      label: 'Minimum number of complex characters',
                      tooltip:
                        'Minimum number of complex or non-alphanumeric characters allowed in a passcode',
                      key: 'minComplexCharsWP',
                      value: 'None',
                      isRequired: false,
                      select: {
                        valueType: 'Integer',
                        options: [
                          {
                            displayingValue: 'None',
                          },
                          {
                            definedValue: '1',
                            displayingValue: '01',
                          },
                          {
                            definedValue: '2',
                            displayingValue: '02',
                          },
                          {
                            definedValue: '3',
                            displayingValue: '03',
                          },
                          {
                            definedValue: '4',
                            displayingValue: '04',
                          },
                          {
                            definedValue: '5',
                            displayingValue: '05',
                          },
                        ],
                      },
                    },
                    {
                      label: 'Maximum passcode age in days',
                      tooltip:
                        'Number of days after which a passcode must be changed',
                      key: 'maxPINAgeInDaysWP',
                      value: null,
                      isRequired: false,
                      input: {
                        type: 'TextArea',
                        placeholderValue:
                          'Should be in between 1-to-730 days or 0 for none',
                        regEx:
                          '^(?:0|[1-9]|[1-9][1-9]|[0-6][0-9][0-9]|7[0-2][0-9]|730)$',
                        validationMessage:
                          'Should be in between 1-to-730 days or 0 for none',
                      },
                    },
                    {
                      label: 'Passcode history',
                      tooltip:
                        'Number of consequent unique passcodes to be used before reuse',
                      key: 'pinHistoryWP',
                      value: null,
                      isRequired: false,
                      input: {
                        type: 'TextArea',
                        placeholderValue:
                          'Should be in between 1-to-50 passcodes or 0 for none',
                        regEx: '^(?:0|[1-9]|[1-4][0-9]|50)$',
                        validationMessage:
                          'Should be in between 1-to-50 passcodes or 0 for none',
                      },
                    },
                    {
                      label: 'Maximum number of failed attempts',
                      tooltip:
                        'The maximum number of incorrect password entries allowed. If the correct password is not entered within the allowed number of attempts, the data on the device will be erased.',
                      value: 'None',
                      key: 'maxFailedAttemptsWP',
                      isRequired: false,
                      select: {
                        valueType: 'Integer',
                        options: [
                          {
                            definedValue: 'None',
                            displayingValue: 'None',
                          },
                          {
                            definedValue: '3',
                            displayingValue: '03',
                          },
                          {
                            definedValue: '4',
                            displayingValue: '04',
                          },
                          {
                            definedValue: '5',
                            displayingValue: '05',
                          },
                          {
                            definedValue: '6',
                            displayingValue: '06',
                          },
                          {
                            definedValue: '7',
                            displayingValue: '07',
                          },
                          {
                            definedValue: '8',
                            displayingValue: '08',
                          },
                          {
                            definedValue: '9',
                            displayingValue: '09',
                          },
                          {
                            definedValue: '10',
                            displayingValue: '10',
                          },
                        ],
                      },
                    },
                  ],
                  conditionList: {
                    conditions: [
                      {
                        name: 'passcodePolicyWPExist',
                        value: true,
                      },
                    ],
                  },
                },
              ],
            },
          ],
        },
      ],
    },
  ],
};

export default policyUIConfigurations;
