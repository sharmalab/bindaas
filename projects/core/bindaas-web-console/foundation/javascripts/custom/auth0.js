let auth0 = null;


//FIXME read values from file
const configureClient = async () => {
    auth0 = await createAuth0Client({
        domain: 'tushar-97.auth0.com',
        client_id: 'hMzraEXa8PpB6glNy1kumbtN44pAKgzt',
        audience: 'my-api',
        prompt: 'login'
    });
};


window.onload = async () => {
    await configureClient();

    const isAuthenticated = await auth0.isAuthenticated();

    if (isAuthenticated) {
        document.getElementById("accessToken").value = await auth0.getTokenSilently();
        document.getElementById("userProfile").value = JSON.stringify(
            await auth0.getUser()
        );

        logout();
        document.getElementById("auth0-form").submit();
    }

    updateUI();

    const query = window.location.search;

    if (query.includes("code=") && query.includes("state=")) {

        await auth0.handleRedirectCallback();

        updateUI();

        window.history.replaceState({}, document.title, "/");

    }
};


const updateUI = async () => {
    const isAuthenticated = await auth0.isAuthenticated();

    if (isAuthenticated) {

        document.getElementById("accessToken").value = await auth0.getTokenSilently();
        document.getElementById("userProfile").value = JSON.stringify(
            await auth0.getUser()
        );

        logout();
        document.getElementById("auth0-form").submit();

    }
};


const login = async () => {
    await auth0.loginWithRedirect({
        redirect_uri: window.location.href
    });

};


const logout = () => {
    auth0.logout({
        returnTo: window.location.href
    });
};


