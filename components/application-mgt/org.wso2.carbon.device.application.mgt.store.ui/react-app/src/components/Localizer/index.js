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

import React from 'react';
import {
  initializeI18n,
  supportedLocals,
} from '../../services/internationalization/i18n';
import { ConfigProvider } from 'antd';
import { I18nextProvider } from 'react-i18next';

const LocaleContext = React.createContext();

class Localizer extends React.Component {
  constructor(props) {
    super(props);
    let currentLocale = localStorage.getItem('locale');
    if (
      currentLocale == null ||
      !supportedLocals.hasOwnProperty(currentLocale)
    ) {
      currentLocale = 'enUS';
      localStorage.setItem('locale', currentLocale);
    }
    this.i18nInstance = initializeI18n();
    this.i18nInstance.changeLanguage(currentLocale);
    this.state = {
      currentLocale,
      changeLocale: this.changeLocale,
    };
  }
  changeLocale = key => {
    if (supportedLocals.hasOwnProperty(key)) {
      this.i18nInstance.changeLanguage(key);
      localStorage.setItem('locale', key);
      this.setState({ currentLocale: key });
    }
  };

  render() {
    return (
      <LocaleContext.Provider value={this.state}>
        <I18nextProvider i18n={this.i18nInstance}>
          <ConfigProvider
            locale={supportedLocals[this.state.currentLocale].antLocale}
          >
            {this.props.children}
          </ConfigProvider>
        </I18nextProvider>
      </LocaleContext.Provider>
    );
  }
}
export { Localizer, LocaleContext };
