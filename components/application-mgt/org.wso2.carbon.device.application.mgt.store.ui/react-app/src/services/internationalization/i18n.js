/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import txtEnUS from './locales/en_US.json';
import antEnUS from 'antd/es/locale/en_US';
import txtJaJP from './locales/ja_JP.json';
import antJaJp from 'antd/es/locale/ja_JP';

// Add new locales to this object
const supportedLocals = {
  enUS: {
    displayText: '🇺🇸 English',
    textTranslations: txtEnUS,
    antLocale: antEnUS,
  },
  jaJP: {
    displayText: '🇯🇵 Japanese',
    textTranslations: txtJaJP,
    antLocale: antJaJp,
  },
};
const initializeI18n = () => {
  i18n.use(initReactI18next).init({
    resources: {
      enGB: {
        translations: supportedLocals.enUS.textTranslations,
      },
      jaJP: {
        translations: supportedLocals.jaJP.textTranslations,
      },
    },
    fallbackLng: 'enUS',
    debug: true,
    ns: ['translations'],
    defaultNS: 'translations',
    interpolation: {
      escapeValue: false,
      formatSeparator: ',',
    },
    react: {
      wait: true,
    },
  });
  return i18n;
};
export { initializeI18n, supportedLocals };
