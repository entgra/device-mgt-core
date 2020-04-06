import react from 'react';
import { withConfigContext } from '../context/ConfigContext';

class Authorized extends react.Component {
  constructor(props) {
    super(props);
  }

  isAuthorized = (user, permission) => {
    if (!user || !permission) {
      return false;
    }
    return user.permissions.includes(permission);
  };

  render() {
    return this.isAuthorized(this.props.context.user, this.props.permission)
      ? this.props.yes
      : this.props.no;
  }
}

Authorized.defaultProps = {
  yes: () => null,
  no: () => null,
};
export default withConfigContext(Authorized);
