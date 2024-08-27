import { Button } from '@vaadin/react-components';
import { useSignal } from '@vaadin/hilla-react-signals';
import { SecureNumberSignalService } from 'Frontend/generated/endpoints.js';
import { useAuth } from 'Frontend/util/auth.js';
import { useNavigate } from 'react-router-dom';

const userCounter = SecureNumberSignalService.userCounter();
const adminCounter = SecureNumberSignalService.adminCounter();

export default function SharedNumberSignal() {
  const navigate = useNavigate();
  const { state, logout } = useAuth();

  const userCounterFromServer = useSignal<number | undefined>(0);
  const adminCounterFromServer = useSignal<number | undefined>(0);

  function login() {
    navigate('/login');
  }

  return (
    <>
      <div>
        {state.user !== undefined ? (
          <>
            <span id="userSpan">{state.user.name}</span>
            <Button id="logoutBtn" onClick={logout}>
              Logout
            </Button>
          </>
        ) : (
          <>
            <span id="userSpan">Anonymous User</span>
            <Button id="loginBtn" onClick={login}>
              Login
            </Button>
          </>
        )}
      </div>
      <div>
        <span id="userCounter">{userCounter}</span>
        <Button id="increaseUserCounter" onClick={() => userCounter.value++}>
          Increment
        </Button>
        <br />
        <span id="adminCounter">{adminCounter}</span>
        <Button id="incrementAdminCounter" onClick={() => adminCounter.value++}>
          Increment
        </Button>
        <br />
        <span id="UserCounterFromServer">{userCounterFromServer}</span>
        <Button
          id="fetchUserCounter"
          onClick={async () => {
            userCounterFromServer.value = await SecureNumberSignalService.fetchUserCounterValue();
          }}
        >
          Fetch User Counter from server
        </Button>
        <br />
        <span id="AdminCounterFromServer">{adminCounterFromServer}</span>
        <Button
          id="fetchAdminCounter"
          onClick={async () => {
            adminCounterFromServer.value = await SecureNumberSignalService.fetchAdminCounterValue();
          }}
        >
          Fetch Admin counter value from server
        </Button>
        <br />
        <Button
          id="reset"
          onClick={async () => {
            await SecureNumberSignalService.resetCounters();
          }}
        >
          Reset
        </Button>
      </div>
    </>
  );
}
